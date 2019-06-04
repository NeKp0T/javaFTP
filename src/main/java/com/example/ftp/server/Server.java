package com.example.ftp.server;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.ftp.server.RequestStatus.*;

/**
 * FPT Server that processes two requests:
 * list — listing files in a directory on the server
 * get — download file from server
 */
public class Server {

    /**
     * ThreadPool to do client request
     */
    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Thread for accepting clients
     */
    private Thread acceptingThread;

    /**
     * Thread for selector processing read requests
     */
    private Thread readingThread;

    /**
     * Thread for selector processing write requests
     */
    private Thread writhingThread;

    /**
     * Listen socket address
     */
    private InetSocketAddress address;

    /**
     * Selector for processing read requests
     */
    private Selector readingSelector;

    /**
     * Selector for processing write requests
     */
    private Selector writingSelector;

    /**
     * Server Socket Channel
     */
    private ServerSocketChannel serverChannel;


    /**
     * Constructor for server
     * @param address IP address of client
     * @param port PORT in server to listen to
     */
    public Server(String address, int port) {
        this.address = new InetSocketAddress(address, port);
    }

    /**
     * Task for thread accepting new clients
     */
    private class AcceptTask implements Runnable {

        @Override
        public void run() {
            try {
                serverChannel = ServerSocketChannel.open();
                serverChannel.socket().bind(address);
                while (!Thread.interrupted()) {
                    accept();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * States of selector
     */
    private enum SelectorMode {
        WRITE, READ
    }

    /**
     * Task for thread processing read/write requests
     */
    private class ReadWriteTask implements Runnable {

        /**
         * Read/write selector to get requests
         */
        private Selector selector;

        /**
         * Mode of the selector
         */
        private SelectorMode mode;

        ReadWriteTask(Selector selector, SelectorMode mode) {
            this.selector = selector;
            this.mode = mode;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    int readyCount = selector.select(1000);
                    if (readyCount == 0) {
                        continue;
                    }

                    var readyKeys = selector.selectedKeys();
                    var iterator = readyKeys.iterator();

                    while (iterator.hasNext()) {
                        var key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        switch (mode) {
                            case READ:
                                if (key.isReadable()) {
                                    read(key);
                                }
                            case WRITE:
                                if (key.isWritable()) {
                                    write(key);
                                }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Task for client request submission
     */
    private class RequestTask implements Runnable {

        /**
         * Information about client
         */
        private ClientInfo clientInfo;

        public RequestTask(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
        }

        @Override
        public void run() {
            try {
                clientInfo.submit();
                clientInfo.status = WRITING;
                clientInfo.channel.register(writingSelector, SelectionKey.OP_WRITE, clientInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starting server (starting three working threads and creating two selectors
     * @throws IOException when can't open the selector
     */
    public void start() throws IOException {
        readingSelector = Selector.open();
        writingSelector = Selector.open();
        readingThread = new Thread(new ReadWriteTask(readingSelector, SelectorMode.READ));
        readingThread.start();
        writhingThread = new Thread(new ReadWriteTask(writingSelector, SelectorMode.WRITE));
        writhingThread.start();
        acceptingThread = new Thread(new AcceptTask());
        acceptingThread.start();
    }

    /**
     * Accept new clients and subscribe them for reading
     * @throws IOException when can't accept new client
     */
    private void accept() throws IOException {
        var channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(readingSelector, SelectionKey.OP_READ, new ClientInfo(channel));
    }

    /**
     * Read request from client
     * @param key identifier for client in selector
     * @throws IOException when can't close client channel
     */
    private void read(SelectionKey key) throws IOException {
        var clientInfo = (ClientInfo) key.attachment();

        switch (clientInfo.status) {
            case READING:
                clientInfo.read();
            case READ_FINISHED:
                clientInfo.status = SUBMITTING;
                service.submit(new Server.RequestTask(clientInfo));
                key.interestOpsAnd(~SelectionKey.OP_READ);
                break;
            case FAILED:
                clientInfo.channel.close();
                break;
        }
    }

    /**
     * Write result of request to client
     * @param key identifier for client in selector
     * @throws IOException when can't close client channel
     */
    private void write(SelectionKey key) throws IOException {
        //System.out.println("write started");
        var clientInfo = (ClientInfo) key.attachment();

        switch (clientInfo.status) {
            case WRITING:
                clientInfo.write();
            case WRITE_FINISHED:
                clientInfo.finishWriting();
                key.interestOpsAnd(~SelectionKey.OP_WRITE);
                break;
            case FAILED:
                clientInfo.channel.close();
                break;
        }
    }

    /**
     * List all files in directory with given path in format:
     * size: Int (name: String is_dir: Boolean)*
     * @param path to list files in it
     * @return string with all listed files in formal
     */
    public static String list(String path) {
        var message = new StringBuilder();
        var directory = new File(path);
        File[] files;
        if (directory.exists()) {
            if (directory.isFile()) {
                files = new File[]{directory};
            } else {
                files = directory.listFiles();
            }
            assert files != null;
            message.append(files.length);
            for (var file : files) {
                message.append(" (");
                message.append(file.getName()).append(" ");
                message.append(file.isDirectory() ? 1 : 0);
                message.append(")");
            }
        } else {
            message.append(-1);
        }

        return message.toString();
    }

    /**
     * Write file content into string
     * @param fileName to write content from
     * @return string with content of given file
     * @throws IOException when an error occurred while reading/writing/opening files
     */
    public static String getFileContent(String fileName) throws IOException {
        var reader = new BufferedReader(new FileReader(fileName));
        var stringBuilder = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }

    /**
     * Get content and size of given file in format:
     * size: Long content: Bytes
     * @param path to file to get information
     * @return string with size and content for given file in format
     */
    public static String get(String path) {
        String message = "";
        try {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                message += file.getTotalSpace() + " ";
                message += getFileContent(path);
            } else {
                message += -1;
            }
        } catch (IOException e) {
            message += -1;
        }
        return message;
    }
}

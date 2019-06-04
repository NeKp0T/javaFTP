package com.example.ftp.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.ftp.client.RequestStatus.*;

public class Server {

    /**
     * Port
     */
    private int port;

    /**
     * ThreadPool to do client request
     */
    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * Thread to accept clients
     */
    private Thread acceptingThread;

    /**
     * Thread for read selector
     */
    private Thread readingThread;

    /**
     * Thread to write selector
     */
    private Thread writhingThread;

    /**
     * Listen socket address
     */
    private InetSocketAddress address;

    /**
     * Selector for reading
     */
    private Selector readingSelector;

    /**
     * Selector for writing
     */
    private Selector writingSelector;


    public Server(String address, int port) {
        this.port = port;
        this.address = new InetSocketAddress(address, port);
    }

    private class AcceptTask implements Runnable {

        @Override
        public void run() {
            try {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.socket().bind(address);
                while (!Thread.interrupted()) {
                    accept(serverChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private enum SelectorMode {
        WRITE, READ;
    }

    private class ReadWriteTask implements Runnable {

        private Selector selector;

        private SelectorMode mode;

        ReadWriteTask(Selector selector, SelectorMode mode) {
            this.selector = selector;
            this.mode = mode;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    int readyCount = selector.select();
                    if (readyCount == 0) {
                        continue;
                    }

                    var readyKeys = selector.selectedKeys();
                    var iterator = readyKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        switch (mode) {
                            case READ:
                                read(key);
                            case WRITE:
                                write(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class RequestTask implements Runnable {

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

    public void start() throws IOException {
        readingSelector = Selector.open();
        writingSelector = Selector.open();
        acceptingThread = new Thread(new AcceptTask());
        acceptingThread.start();
        readingThread = new Thread(new ReadWriteTask(readingSelector, SelectorMode.READ));
        readingThread.start();
        writhingThread = new Thread(new ReadWriteTask(writingSelector, SelectorMode.WRITE));
        writhingThread.start();
    }

    private void accept(ServerSocketChannel serverChannel) throws IOException {
        System.out.println("accept started");
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        channel.register(readingSelector, SelectionKey.OP_READ, new ClientInfo(channel));
    }

    private void read(SelectionKey key) throws IOException {
        System.out.println("read started");
        ClientInfo clientInfo = (ClientInfo) key.attachment();

        if (clientInfo.status == READING) {
            clientInfo.read();
        }

        if (clientInfo.status == READ_FINISHED) {
            clientInfo.status = SUBMITTING;
            service.submit(new Server.RequestTask(clientInfo));
        }
    }

    private void write(SelectionKey key) throws IOException {
        System.out.println("write started");
        ClientInfo clientInfo = (ClientInfo) key.attachment();

        if (clientInfo.status == WRITING) {
            clientInfo.write();
        }

        if (clientInfo.status == WRITE_FINISHED) {
            //clientInfo.request.status = NONE; //TODO unsubscribe
        }
    }

    public static String list(String path) {
        String message = "";
        File directory = new File(path);
        File[] files;
        if (directory.exists()) {
            if (directory.isFile()) {
                files = new File[]{directory};
            } else {
                files = directory.listFiles();
            }
            message += files.length;
            for (var file : files) {
                message += " (";
                message += file.getName() + " ";
                message += file.isDirectory() ? 1 : 0;
                message += ")";
            }
        } else {
            message += -1;
        }

        return message;
    }

    public static String readUsingBufferedReader(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


    public static String get(String path) throws IOException {
        String message = "";
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            message += file.getTotalSpace() + " ";
            message += readUsingBufferedReader(path);
        } else {
            message += -1;
        }
        return message;
    }
}

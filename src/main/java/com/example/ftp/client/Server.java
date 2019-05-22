package com.example.ftp.client;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private final static int BUFFER_SIZE = 8192;

    /**
     * Port
     */
    private int port = 2599;

    /**
     * Host
     */
    private String HOST = "127.0.0.1";

    /**
     * Listen socket address
     */
    private InetSocketAddress address;

    private Selector selector;

    public Server(String address, int port) {
        this.port = port;
        this.address = new InetSocketAddress(address, port);
    }

    public void startServer() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);


        serverChannel.socket().bind(address);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server started");
        while (true) {
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();

                // Remove key from set so we don't process it twice
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) { // Accept client connections
                    accept(key);
                } else if (key.isReadable()) { // Read from client
                    read(key);
                } else if (key.isWritable()) {
                    //write(key);
                }
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        SocketChannel newChannel = ((ServerSocketChannel) key.channel()).accept();
        newChannel.configureBlocking(false);
        newChannel.register(key.selector(), SelectionKey.OP_READ);

    }

    private void read(SelectionKey key) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        while (!decoder.decode(buffer).toString().contains("#")) {
            channel.read(buffer);
        }
        channel.close();
        key.cancel();

        buffer.flip();
        String request = decoder.decode(buffer).toString();
        System.out.println("request: " + request);
        String path = request.substring(2, request.length() - 1);

        int requestType = request.charAt(0);
        switch (requestType) {
            case 1:
                ByteBuffer listRequest = listRequest(path);
                while (listRequest.hasRemaining()) {
                    channel.write(listRequest);
                }
                listRequest.clear();
                break;
            case 2:
                ByteBuffer getRequest = getRequest(path);
                while (getRequest.hasRemaining()) {
                    channel.write(getRequest);
                }
                getRequest.clear();
        }

        buffer.clear();
        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }

    File dir1 = new File("C://SomeDir");

    private ByteBuffer listRequest(String path) {
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

        Charset charset = Charset.forName("ISO-8859-1");

        byte[] messageBytes = message.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(Math.max(messageBytes.length, BUFFER_SIZE));
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }

    private static String readUsingBufferedReader(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader( new FileReader(fileName));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");
        while( ( line = reader.readLine() ) != null ) {
            stringBuilder.append( line );
            stringBuilder.append( ls );
        }

        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


    private ByteBuffer getRequest(String path) throws IOException {
        String message = "";
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            message += file.getTotalSpace() + " ";
            message += readUsingBufferedReader(path);
        } else {
            message += -1;
        }

        Charset charset = Charset.forName("ISO-8859-1");

        byte[] messageBytes = message.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(Math.max(messageBytes.length, BUFFER_SIZE));
        buffer.put(messageBytes);
        buffer.flip();
        return buffer;
    }
}

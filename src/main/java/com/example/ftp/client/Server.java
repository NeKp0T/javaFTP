package com.example.ftp.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private final static int BUFFER_SIZE = 8192;

    /**
     * Port
     */
    private static final int PORT = 2599;

    /**
     * Host
     */
    private String host = "127.0.0.1";

    /**
     * Listen socket address
     */
    private InetSocketAddress address;

    public Server(String addr, int port) {
        host = addr;
    }

    public void startServer() throws IOException {
        address = new InetSocketAddress(host, PORT);

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getByName("localhost"), 2599));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            if (selector.select() <= 0) {
                continue;
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isAcceptable()) { // Accept client connections
                    accept(key);
                }
                if (key.isReadable()) { // Read from client
                    read(key);
                }
            }
        }
//        System.out.println("Miserably failed");
    }

    private void accept(SelectionKey key) throws IOException {
        System.out.println("accept connection");
        SocketChannel newChannel = ((ServerSocketChannel) key.channel()).accept();
        newChannel.configureBlocking(false);
        newChannel.register(key.selector(), SelectionKey.OP_READ);
        System.out.println("accepted connection");
    }

    private void read(SelectionKey key) throws IOException {
//        SocketChannel sc = (SocketChannel) key.channel();
//        ByteBuffer bb = ByteBuffer.allocate(1024);
//
//        int readCnt;
//        do {
//            readCnt = sc.read(bb);
//            System.out.println();
//            String result = new String(bb.array()).trim();
//            System.out.println("Message received: " + result + " Message length= " + result.length());
//        } while (readCnt != 0);
//        System.out.println("That's all");
//
//        return;

        System.out.println("Starting read");
        Charset charset = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = charset.newDecoder();

        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        StringBuilder a = new StringBuilder();
        while (!a.toString().contains("#")) {
            buffer.clear();
            int read = channel.read(buffer);
            String result = new String(buffer.array()).trim();
            System.out.println("Got: " + result);
            if (read != 0) {
                System.out.println("Read " + read + " bytes");
            } else {
                System.out.println("Read 0 bytes");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            a.append(result);
        }

        buffer.flip();
        String request = a.toString();
        System.out.println("request: " + request);

        char requestType = request.charAt(0);
        switch (requestType) {
            case '1':
                listRequest(key, "~/Pictures");
                break;
//            case 2:
//                getRequest(path);
        }
        System.out.println("server done");

        channel.close();
        key.cancel();

        buffer.clear();
    }

    private void listRequest(SelectionKey key, String path) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        String message = "";
        File directory = new File(path);
        File[] files;
        System.out.println("Generating message");
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

        System.out.println("Message generated: " + message);

        Charset charset = Charset.forName("ISO-8859-1");

        byte[] messageBytes = message.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(Math.max(messageBytes.length, BUFFER_SIZE));
        buffer.put(messageBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.clear();

        System.out.println("Message sent");

        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
    }
}

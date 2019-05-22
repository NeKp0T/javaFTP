package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

public class Server {

    private final static int BUFFER_SIZE = 8192;

    /**
     * Port
     */
    private int port;

    /**
     * Host
     */
    private String host;

    /**
     * Listen socket address
     */
    private InetSocketAddress adress;

    private Selector selector;

    private void startServer() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        serverChannel.socket().bind(adress);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server started");

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

    private void accept(SelectionKey key) throws IOException {
        SocketChannel newChannel = ((ServerSocketChannel) key.channel()).accept();
        newChannel.configureBlocking(false);
        newChannel.register(key.selector(), SelectionKey.OP_READ);

    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int numRead = -1;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            Socket socket = channel.socket();
            System.out.println("Connection closed by client: " + socket.getRemoteSocketAddress());
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        System.out.println("Got: " + new String(data));
    }
}

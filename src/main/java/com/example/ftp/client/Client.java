package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client {

    private static final int PORT = 2599;

    private final SocketChannel socketChannel;

    public static Client connect(String address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(address, PORT));

        //noinspection StatementWithEmptyBody
        while (!socketChannel.finishConnect());

        return new Client(socketChannel);
    }

    public void sendRequest(String request) {

    }

    private Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

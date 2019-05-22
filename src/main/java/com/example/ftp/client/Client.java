package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;

public class Client {

    private static final int PORT = 2599;
    private static final int BUFF_SIZE = 200;

    private final SocketChannel socketChannel;

    public static Client connect(String address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(address, PORT));

        //noinspection StatementWithEmptyBody
        while (!socketChannel.finishConnect());

        return new Client(socketChannel);
    }

    public List<String> listRequest(String path) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        String request = "1" + path;

        byte[] requestBytes = request.getBytes(charset);

        ByteBuffer buffer = ByteBuffer.allocate(Math.max(requestBytes.length, BUFF_SIZE));
        buffer.put(requestBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        buffer.clear();

        while (true) {
            CharsetDecoder decoder = charset.newDecoder();

            try {
                int bytesRead = socketChannel.read(buffer);
                //	int bytesread = client.read(buffer);
                if (bytesRead == -1) {
                    //key.cancel();
                    socketChannel.close();
                    // continue;
                }
                buffer.flip();
                String request = decoder.decode(buffer).toString();
                System.out.println("request: " + request);
                buffer.clear();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

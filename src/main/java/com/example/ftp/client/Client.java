package com.example.ftp.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;;

public class Client {

    private static final int PORT = 2599;
    private static final int BUFF_SIZE = 200;

    private final SocketChannel socketChannel;

    public static Client connect(String address) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
//        socketChannel.configureBlocking(false);
        socketChannel.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 2599));

        //noinspection StatementWithEmptyBody
        while (!socketChannel.finishConnect());
        socketChannel.socket().setTcpNoDelay(true);

        return new Client(socketChannel);
    }

    public List<String> listRequest(String path) throws IOException {
        Charset charset = Charset.forName("ISO-8859-1");
        String request = "1 " + path + "#";

        byte[] requestBytes = request.getBytes(charset);

        ByteBuffer buffer = ByteBuffer.allocate(Math.max(requestBytes.length, BUFF_SIZE));
        buffer.put(requestBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
        buffer.clear();
        System.out.println("Client sent");


        var wholeAnswerBuilder = new StringBuilder();

        while (true) {
            CharsetDecoder decoder = charset.newDecoder();

            int bytesRead = socketChannel.read(buffer);
//            if (bytesRead == -1) {
//                socketChannel.close();
//                break;
//            }
            buffer.flip();
            String answer = decoder.decode(buffer).toString();
            wholeAnswerBuilder.append(answer);
            buffer.clear();

            if (wholeAnswerBuilder.toString().length() < 2) {
                break;
            }
        }

        String gotSoFar = wholeAnswerBuilder.toString();
        System.out.println("got " + gotSoFar);

        int size = Integer.parseInt(gotSoFar);
        if (size < 0) {
            return null;
        }

        int lengthRemaining = size - (gotSoFar.length() + Integer.toString(size).length());

        while (true) {
            CharsetDecoder decoder = charset.newDecoder();

            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                break;
            }
            buffer.flip();
            String answer = decoder.decode(buffer).toString();
            wholeAnswerBuilder.append(answer);
            buffer.clear();

            lengthRemaining -= answer.chars().filter(c -> c == (int)')').count();

            if (lengthRemaining == 0) {
                break;
            }
        }

        if (lengthRemaining < 0) {
            System.out.println("Got too much");
        }

        return parseListRequest(wholeAnswerBuilder.toString());
    }

    private static List<String> parseListRequest(String answer) {
        return List.of(answer); // TODO
    }

    private Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

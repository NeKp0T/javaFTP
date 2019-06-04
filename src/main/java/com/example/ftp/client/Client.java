package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;;

public class Client {

    private static final int PORT = 2599;
    private static final int BUFF_SIZE = 200;


    public static Charset charset = Charset.forName("UTF-8");
    public static CharsetEncoder encoder = charset.newEncoder();
    public static CharsetDecoder decoder = charset.newDecoder();

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

        byte[] requestBytes = path.getBytes(charset);

        ByteBuffer buffer = ByteBuffer.allocate(BUFF_SIZE);
        buffer.putInt(1);
        buffer.putInt(path.getBytes().length);
        buffer.put(requestBytes);


        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        buffer.clear();

        System.out.println("Request sent: " + path);

        var wholeAnswerBuilder = new StringBuilder();

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

            if (answer.length() != 0) {
                System.out.println("Got: " + answer);
            }

            if (!answer.chars().allMatch(Character::isDigit)) {
                break;
            }
        }

        String gotSoFar = wholeAnswerBuilder.toString();

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

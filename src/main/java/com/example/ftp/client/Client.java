package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;;

public class Client {

    private static final int PORT = 2599;
    private static final int BUFF_SIZE = 512;


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
        buffer.putInt(requestBytes.length);
        buffer.put(requestBytes);

        System.out.println("Buffer should have " + (8 + requestBytes.length) + " bytes, have " + buffer.position());

        buffer.flip();

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        System.out.println("Request sent: " + path + ", total " + buffer.limit() + "bytes");

        buffer.clear();
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);

        ByteBuffer[] buffers = {sizeBuffer, buffer};

        CharsetDecoder decoder = charset.newDecoder();
        int read = 0;
        int size = -1;
        while (true) {
            int bytesRead = (int) socketChannel.read(buffers); // TODO int long
            if (bytesRead != 0) {
                System.out.println("Client: bytesRead " + bytesRead);
            }
            if (bytesRead == -1) {
                socketChannel.close();
                break;
            }
            read += bytesRead;
            if (size == -1 && read >= sizeBuffer.limit()) {
                sizeBuffer.flip();
                size = sizeBuffer.getInt();
                System.out.println("Client: got size " + size);
            }

            if (size != -1 && read >= size) {
                buffer.flip();
                break;
            }
        }

        String answer = decoder.decode(buffer).toString();

        System.out.println("Client: answer = " + answer);
        return parseListRequest(answer);
    }

    private static List<String> parseListRequest(String answer) {
        return List.of(answer); // TODO
    }

    private Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

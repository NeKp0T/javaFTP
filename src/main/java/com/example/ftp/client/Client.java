package com.example.ftp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
;

public class Client {

    private static final int PORT = 2599;
    private static final int BUFF_SIZE = 128;


    public static Charset charset = Charset.forName("UTF-8");
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

    public ListRequestAnswer listRequest(String path) throws IOException {
        ByteBuffer buffer = sendRequest(1, path);

        buffer = receivePackage(buffer);

        String answer = decoder.decode(buffer).toString();
        return parseListRequest(answer);
    }

    public GetRequestAnswer getRequest(String path) throws IOException {
        ByteBuffer buffer = sendRequest(2, path);

        System.out.println("Client send");

        buffer = receivePackage(buffer);

        System.out.println("Client receive");

        return parseGetRequest(decoder.decode(buffer).toString());
    }

    /**
     * Sends a request of provided type to server allocating a new buffer
     * in process
     * @param type type of request to send
     * @param path path to send
     * @return allocated buffer ready for reuse
     * @throws IOException if write failed
     */
    private ByteBuffer sendRequest(int type, String path) throws IOException {
        byte[] requestBytes = path.getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(requestBytes.length  + 4 * 2);
        buffer.putInt(type);
        buffer.putInt(requestBytes.length);
        buffer.put(requestBytes);

        buffer.flip();

        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }

        buffer.clear();

        return buffer;
    }

    /**
     * Receives a package using provided buffer.
     * Allocates new buffers if provided proves to be not big enough.
     * @param buffer a buffer to use
     * @return a buffer containing full received package
     * @throws IOException if exception happens while reading
     */
    private ByteBuffer receivePackage(ByteBuffer buffer) throws IOException {
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);

        ByteBuffer[] buffers = {sizeBuffer, buffer};

        int read = 0;
        int size = -1;
        while (true) {
            int bytesRead = (int) socketChannel.read(buffers); // TODO int long

            if (buffer.position() == buffer.limit()) {
                // create a new buffer if this one is full
                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.limit() * 2);
                newBuffer.put(buffer.array());
                buffer = newBuffer;
                buffers[1] = newBuffer;
            }

            if (bytesRead == -1) {
//                socketChannel.close();
                break;
            }
            read += bytesRead;
            if (size == -1 && read >= sizeBuffer.limit()) {
                sizeBuffer.flip();
                size = sizeBuffer.getInt();
            }

            if (size != -1 && read >= size) {
                buffer.flip();
                break;
            }
        }
        return buffer;
    }

    private static ListRequestAnswer parseListRequest(String answer) {
        int itemsCount;
        try {
            int intEnd = answer.indexOf(' ');
            if (intEnd == -1) {
                intEnd = answer.length();
            }
            itemsCount = Integer.parseInt(answer.substring(0, intEnd));
        } catch (NumberFormatException e) {
            return new ListRequestAnswer(RequestStatus.CRITICAL_ERROR);
        }

        if (itemsCount == -1) {
            return new ListRequestAnswer(RequestStatus.WRONG_PATH);
        }

        int beginning = answer.indexOf("(");
        String contents = answer.substring(beginning + 1, answer.length() - beginning - 2);

        String[] splitted = contents.split("\\) \\(");

        var status = RequestStatus.OK;
        var result = new ArrayList<FileDescription>();
        for (String s : splitted) {
            FileDescription file = FileDescription.parse(s);
            if (file != null) {
                result.add(file);
            } else {
                status = RequestStatus.ERROR;
            }
        }

        return new ListRequestAnswer(result, status);
    }

    private GetRequestAnswer parseGetRequest(String answer) {
        int longEnd = answer.indexOf(' ');
        if (longEnd == -1) {
            longEnd = answer.length();
        }

        long fileSize;
        try {
            fileSize = Long.parseLong(answer.substring(0, longEnd));
        } catch (NumberFormatException e) {
            return new GetRequestAnswer(0, null, RequestStatus.CRITICAL_ERROR);
        }

        if (fileSize == -1) {
            return new GetRequestAnswer(-1, null, RequestStatus.OK);
        }

        if (longEnd + 1 > answer.length()) {
            return new GetRequestAnswer(fileSize, "", RequestStatus.ERROR);
        }

        return new GetRequestAnswer(fileSize, answer.substring(longEnd + 1), RequestStatus.OK);
    }

    private Client(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}

package com.example.ftp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import static com.example.ftp.client.RequestStatus.READ_FINISHED;
import static com.example.ftp.client.RequestStatus.WRITE_FINISHED;

public class ClientInfo {

    private static final int SMALL_BUFFER_SIZE = 4;
    private static final int BIG_BUFFER_SIZE = 4086;

    public SocketChannel channel;

    public RequestStatus status;

    public static Charset charset = Charset.forName("UTF-8");
    public static CharsetEncoder encoder = charset.newEncoder();
    public static CharsetDecoder decoder = charset.newDecoder();

    public int size;

    public int byteRead;

    public int byteWrite;

    private ByteBuffer idBuffer = ByteBuffer.allocate(SMALL_BUFFER_SIZE);
    private ByteBuffer sizeBuffer = ByteBuffer.allocate(SMALL_BUFFER_SIZE);
    private ByteBuffer pathBuffer = ByteBuffer.allocate(BIG_BUFFER_SIZE);
    private ByteBuffer resultBuffer = ByteBuffer.allocate(BIG_BUFFER_SIZE);
    private ByteBuffer[] bufferRead = {idBuffer, sizeBuffer, pathBuffer};
    private ByteBuffer[] bufferWrite = {sizeBuffer, resultBuffer};

    ClientInfo(SocketChannel channel) {
        this.channel = channel;
    }

    private void cleanBuffers() {
        idBuffer.clear();
        sizeBuffer.clear();
        pathBuffer.clear();
        size = 0;
    }

    public void read() throws IOException {

        byteRead += channel.read(bufferRead);

        if (size == -1 && byteRead >= 2 * SMALL_BUFFER_SIZE) {
            size = sizeBuffer.getInt();
            byteRead -= 2 * SMALL_BUFFER_SIZE;
        }

        if (size != -1 && byteRead >= size) {
            status = READ_FINISHED;
            System.out.println("request received");
        }
    }

    public void write() throws IOException {
        byteWrite += channel.write(bufferWrite);
        if (size == byteWrite) {
            status = WRITE_FINISHED;
            System.out.println("result done");
            cleanBuffers();
        }
    }

    public void submit() throws IOException {
        var id = idBuffer.getInt();
        var path = decoder.decode(pathBuffer).toString();
        String message = "";
        switch (id) {
            case 1 :
                message = Server.list(path);
                break;
            case 2 :
                message = Server.get(path);
                break;
        }
        sizeBuffer = encoder.encode(CharBuffer.wrap(String.valueOf(message.getBytes().length)));
        resultBuffer = encoder.encode(CharBuffer.wrap(message));
        size = message.getBytes().length + 4;
    }
}

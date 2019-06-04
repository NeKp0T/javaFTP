package com.example.ftp.server;

import com.example.ftp.server.RequestStatus;
import com.example.ftp.server.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import static com.example.ftp.server.RequestStatus.*;

public class ClientInfo {

    private static final int SMALL_BUFFER_SIZE = 4;
    private static final int BIG_BUFFER_SIZE = 4086;

    public SocketChannel channel;

    public RequestStatus status;

    public static Charset charset = Charset.forName("UTF-8");
    public static CharsetEncoder encoder = charset.newEncoder();
    public static CharsetDecoder decoder = charset.newDecoder();

    public int size = -1;

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
        status = READING;
    }

    private void cleanBuffers() {
        idBuffer.clear();
        sizeBuffer.clear();
        pathBuffer.clear();
        resultBuffer.clear();
        size = -1;
        byteRead = 0;
        byteWrite = 0;
    }

    public void read() throws IOException {
        byteRead += channel.read(bufferRead); // TODO if it returns -1

        if (size == -1 && byteRead >= 2 * SMALL_BUFFER_SIZE) {
            sizeBuffer.flip();
            idBuffer.flip();
            size = sizeBuffer.getInt();
            byteRead -= 2 * SMALL_BUFFER_SIZE;
        }

        if (size != -1 && byteRead >= size) {
            status = READ_FINISHED;
            pathBuffer.flip();
        }
    }

    public void write() throws IOException {
        byteWrite += channel.write(bufferWrite);
        if (size == byteWrite) {
            status = WRITE_FINISHED;
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
            default:
                System.out.println("ClientInfo: Unknown id"); // TODO delete debug output
                throw new IOException("Got wrong id"); // TODO mb make the exception class
        }

        byte[] result = message.getBytes(charset);

        size = result.length + sizeBuffer.limit();
        sizeBuffer.clear();
        sizeBuffer.putInt(result.length);
        sizeBuffer.flip();
//        sizeBuffer = encoder.encode(CharBuffer.wrap(String.valueOf(message.getBytes().length)));
        resultBuffer.clear(); // probably already clear TODO delete
        resultBuffer.put(result);
        resultBuffer.flip();
    }

    public void finishWriting() {
        cleanBuffers();
        status = READING;
    }
}

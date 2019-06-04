package com.example.ftp.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import static com.example.ftp.client.RequestStatus.*;

/**
 * Class with information about
 */
public class ClientInfo {

    /**
     * Size for buffer to store int
     */
    private static final int SMALL_BUFFER_SIZE = 4;

    /**
     * Size for buffer to store String
     */
    private static final int BIG_BUFFER_SIZE = 4086;

    /**
     * Channel to deal with client
     */
    public SocketChannel channel;

    /**
     * Status of dealing with client process
     */
    public RequestStatus status;

    /**
     * Charset to have stable code
     */
    private static Charset charset = Charset.forName("UTF-8");

    /**
     * Decoder for client request
     */
    private static CharsetDecoder decoder = charset.newDecoder();

    /**
     * Size of message to read/write to channel
     */
    private int size = -1;

    /**
     * Number of already read bytes
     */
    private int byteRead;

    /**
     * Number of already write bytes
     */
    private int byteWrite;

    /**
     * Buffer to read identifier for request task
     */
    private ByteBuffer idBuffer = ByteBuffer.allocate(SMALL_BUFFER_SIZE);

    /**
     * Buffer to read/write size of message
     */
    private ByteBuffer sizeBuffer = ByteBuffer.allocate(SMALL_BUFFER_SIZE);

    /**
     * Buffer to read path for request task
     */
    private ByteBuffer pathBuffer = ByteBuffer.allocate(BIG_BUFFER_SIZE);

    /**
     * Buffer to write a result of request
     */
    private ByteBuffer resultBuffer = ByteBuffer.allocate(BIG_BUFFER_SIZE);

    /**
     * Array of buffers to read request
     */
    private ByteBuffer[] bufferRead = {idBuffer, sizeBuffer, pathBuffer};

    /**
     * Array of buffers to write result of request
     */
    private ByteBuffer[] bufferWrite = {sizeBuffer, resultBuffer};

    ClientInfo(SocketChannel channel) {
        this.channel = channel;
        status = READING;
    }

    private void clean() {
        idBuffer.clear();
        sizeBuffer.clear();
        pathBuffer.clear();
        resultBuffer.clear();
        size = -1;
        byteRead = 0;
        byteWrite = 0;
    }

    /**
     * Read request from client
     */
    public void read() {
        try {
            byteRead += channel.read(bufferRead);
            System.out.println("Serv: Read " + byteRead + " bytes");

            if (size == -1 && byteRead >= 2 * SMALL_BUFFER_SIZE) {
                sizeBuffer.flip();
                idBuffer.flip();
                size = sizeBuffer.getInt();
                byteRead -= 2 * SMALL_BUFFER_SIZE;

                System.out.println("Serv: Size " + size);
            }

            if (size != -1 && byteRead >= size) {
                status = READ_FINISHED;
                pathBuffer.flip();
                System.out.println("request received");
            }
        } catch (IOException e) {
            status = FAILED;
            clean();
            System.out.println("failed read");
        }
    }

    public void write() {
        try {
            byteWrite += channel.write(bufferWrite);
            if (size == byteWrite) {
                status = WRITE_FINISHED;
                System.out.println("result done");
                clean();
            }
        } catch (IOException e) {
            status = FAILED;
            clean();
            System.out.println("failed write");
        }
    }

    public void finishWriting() {
        clean();
        status = READING;
    }

    public void submit() {
        try {
            var id = idBuffer.getInt();
            var path = decoder.decode(pathBuffer).toString();
            System.out.println("Serv: Got path " + path + ", id " + id);

            String message;
            switch (id) {
                case 1:
                    message = Server.list(path);
                    break;
                case 2:
                    message = Server.get(path);
                    break;
                default:
                    message = "-1";
                    System.out.println("Unknown id");
            }

        byte[] result = message.getBytes(charset);

        size = resultBuffer.limit() + sizeBuffer.limit();

        sizeBuffer.clear();
        sizeBuffer.putInt(result.length);
        sizeBuffer.flip();
        resultBuffer.clear(); // probably already clear TODO delete
        resultBuffer.put(result);
        resultBuffer.flip();

        System.out.println("Serv: generated message size: " + size);
        } catch (CharacterCodingException e) {
            clean();
            status = FAILED;
            System.out.println("submit write");
        }
    }
}

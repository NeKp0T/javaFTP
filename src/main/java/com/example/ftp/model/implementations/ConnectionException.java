package com.example.ftp.model.implementations;

import java.io.IOException;

/**
 * An exception occurring if <code>DirectoryModelImpl</code> can not establish
 * connection to server.
 */
public class ConnectionException extends Exception {
    /**
     * Constructs a new ConnectionException
     * @param e exception that happened while connecting
     */
    ConnectionException(IOException e) {
        super(e);
    }
}

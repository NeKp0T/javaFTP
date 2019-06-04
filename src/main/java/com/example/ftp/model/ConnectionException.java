package com.example.ftp.model;

import java.io.IOException;

public class ConnectionException extends Exception {
    public ConnectionException(Exception e) {
        super(e);
    }
}

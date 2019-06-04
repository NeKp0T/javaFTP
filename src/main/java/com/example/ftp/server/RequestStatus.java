package com.example.ftp.server;

public enum  RequestStatus {
    READING, SUBMITTING, WRITING,
    READ_FINISHED, WRITE_FINISHED;
}

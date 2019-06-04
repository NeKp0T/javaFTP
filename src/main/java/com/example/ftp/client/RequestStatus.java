package com.example.ftp.client;

/**
 * Status of dealing with client process
 */
public enum  RequestStatus {
    READING, SUBMITTING, WRITING,
    READ_FINISHED, WRITE_FINISHED, FAILED
}

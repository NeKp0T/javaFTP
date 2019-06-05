package com.example.ftp.model.implementations;

import com.example.ftp.model.DirectoryView;

/**
 * The most basic implementation of FileView and DirectoryView in one class
 */
public class FileViewImpl implements DirectoryView {

    private final String name;

    /**
     * Constructs a new FileViewImpl with provided name
     * @param name name of file to construct
     */
    public FileViewImpl(String name) {
        this.name = name;
    }

    /**
     * Returns name of this file. Guaranteed to be the same every time
     * @return file's name
     */
    @Override
    public String getName() {
        return name;
    }
}

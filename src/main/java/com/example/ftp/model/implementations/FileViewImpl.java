package com.example.ftp.model.implementations;

import com.example.ftp.model.DirectoryView;

public class FileViewImpl implements DirectoryView {

    private final String name;

    public FileViewImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

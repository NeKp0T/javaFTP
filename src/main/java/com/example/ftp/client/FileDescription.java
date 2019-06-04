package com.example.ftp.client;

import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FileDescription {
    private String name;
    private boolean directory;

    @Nullable
    public static FileDescription parse(String s) {
        if (s.length() < 3) {
            return null;
        }

        char lastChar = s.charAt(s.length() - 1);
        String name = s.substring(0, s.length() - 2);

        if (lastChar == '1') {
            return new FileDescription(name, true);
        }
        if (lastChar == '0') {
            return new FileDescription(name, false);
        }
        return null;
    }

    public FileDescription(String name, boolean directory) {
        this.name = name;
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        if (directory) {
            return name + File.separator;
        } else {
            return name;
        }
    }
}

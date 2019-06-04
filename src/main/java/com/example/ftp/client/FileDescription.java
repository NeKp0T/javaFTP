package com.example.ftp.client;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Holder for file(directory) description
 */
public class FileDescription {
    /**
     * Name of file
     */
    private String name;

    /**
     * True if describing file is directory, false otherwise
     */
    private boolean isDirectory;

    /**
     * Parse file description from the string
     * @param description string with description
     * @return Object with file description
     */
    @Nullable
    public static FileDescription valueOf(String description) {
        if (description.length() < 3) {
            return null;
        }

        char lastChar = description.charAt(description.length() - 1);
        String name = description.substring(0, description.length() - 2);

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
        this.isDirectory = directory;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Convert file description to string
     * @return string with description
     */
    @Override
    public String toString() {
        if (isDirectory) {
            return name + File.separator;
        } else {
            return name;
        }
    }
}

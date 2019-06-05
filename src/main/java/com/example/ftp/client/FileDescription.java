package com.example.ftp.client;

import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Holder for file (or directory) description
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
    public static FileDescription parse(String description) {
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

    /**
     * Constructs a new FileDescription with provided parameters
     * @param name      name of a described file
     * @param directory whether this file is a directory
     */
    public FileDescription(String name, boolean directory) {
        this.name = name;
        this.isDirectory = directory;
    }

    /**
     * A gettor for a file's name
     * @return file's name
     */
    public String getName() {
        return name;
    }

    /**
     * Determines whether it is a directory
     * @return whether file a directory
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Convert file description to a string
     * @return file's name or file's name + '/' if it is a directory
     */
    @Override
    public String toString() {
        if (isDirectory) {
            return name + "/";
        } else {
            return name;
        }
    }
}

package com.example.ftp.model.implementations;

import com.example.ftp.client.*;
import com.example.ftp.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of DirectoryModel that uses Server as a filesystem provider.
 *
 * Since protocol does not provide any means of determining server's file separator it
 * assumes separator to be <code>'/'</code>.
 */
public class DirectoryModelImpl implements DirectoryModel {
    private final Client client;
    private List<FileView> files;
    private List<DirectoryView> directories;

    private String currentPath;

    /**
     * Constructs a new DirectoryModelImpl connected to a server running on a provided address.
     * @param address address of a server to connect
     * @throws ConnectionException if could not connect to a server
     */
    public DirectoryModelImpl(String address) throws ConnectionException {
        try {
            client = Client.connect(address);
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
        files = new ArrayList<>();
        directories = new ArrayList<>();
        currentPath = "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<FileView> getFiles() {
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DirectoryView> getDirectories() {
        return directories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetRequestAnswer getFileContents(int fileIndex) {
        if (fileIndex < 0 || fileIndex >= getFiles().size()) {
            return GetRequestAnswer.error();
        }
        try {
            return client.getRequest(getPath(getFiles().get(fileIndex)));
        } catch (IOException e) {
            return GetRequestAnswer.error();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenResult openDirectoryByAbsolutePath(String path) {

        ListRequestAnswer answer;
        try {
            answer = client.listRequest(path);
        } catch (IOException e) {
            return OpenResult.FAIL;
        }

        if (answer.status == RequestStatus.CRITICAL_ERROR) {
            return OpenResult.FAIL;
        }

        if (answer.status == RequestStatus.WRONG_PATH) {
            return OpenResult.WRONG_PATH;
        }

        currentPath = path;
        directories = answer.files.stream()
                .filter(FileDescription::isDirectory)
                .map(f -> new FileViewImpl(f.toString()))
                .collect(Collectors.toList());
        files = answer.files.stream()
                .filter(fd -> !fd.isDirectory())
                .map(f -> new FileViewImpl(f.toString()))
                .collect(Collectors.toList());
        return OpenResult.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenResult openDirectory(int directoryIndex) {
        if (directoryIndex == -1) {
            return up();
        }
        if (directoryIndex < 0 || directoryIndex >= getDirectories().size()) {
            return OpenResult.WRONG_PATH;
        }
        return openDirectoryByAbsolutePath(getPath(getDirectories().get(directoryIndex)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenResult reload() {
        return openDirectoryByAbsolutePath(currentPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenResult openSubdirectory(String name) {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        return openDirectoryByAbsolutePath(currentPath + name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenResult up() {
        if (currentPath.length() == 0) {
            return OpenResult.WRONG_PATH;
        }
        int prevSeparator = currentPath
                .substring(0, currentPath.length() - 1)
                .lastIndexOf('/');
        if (prevSeparator == -1) {
            return OpenResult.WRONG_PATH;
        }
        return openDirectoryByAbsolutePath(currentPath.substring(0, prevSeparator + 1));
    }

    private String getPath(FileView file) {
        return currentPath + file.getName();
    }
}

package com.example.ftp.model;

import com.example.ftp.client.GetRequestAnswer;

import java.util.List;

public interface DirectoryModel {
    /**
     * A getter for list of files in current directory which are not directories.
     * @return list of files in current directory
     */
    List<FileView> getFiles();
    /**
     * A getter for list of subdirectories of current directory.
     * @return list of subdirectories of current
     */
    List<DirectoryView> getDirectories();

    GetRequestAnswer getFileContents(int fileIndex);

    OpenResult openDirectory(int directoryIndex);

    OpenResult openDirectoryByPath();

    void reload();

}

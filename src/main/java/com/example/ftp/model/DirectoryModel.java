package com.example.ftp.model;

import com.example.ftp.client.GetRequestAnswer;

import java.util.List;

// TODO docs
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

    /**
     * Calls get request for <code>i</code>-th file in directory.
     * If <code>i</code> is out of bounds, returns critical error.
     * @param fileIndex index of file in <code>getFiles()</code> to get
     * @return get request result
     */
    GetRequestAnswer getFileContents(int fileIndex);

    /**
     * Returns path to currently opened directory
     * @return current path
     */
    String getCurrentPath();

    /**
     * Tries to open i-th directory on the list.
     * @param directoryIndex index of directory in <code>getDirectories()</code> to open
     * @return information on success
     */
    OpenResult openDirectory(int directoryIndex);

    /**
     * Tries to open a directory by an absolute path.
     * @param path path to open
     * @return information on success
     */
    OpenResult openDirectoryByAbsolutePath(String path);

    /**
     * Tries to open a subdirectory by name.
     * @param name path to open
     * @return information on success
     */
    OpenResult openSubdirectory(String name);

    /**
     * Tries to open parent directory
     * @return information on success
     */
    OpenResult up();

    /**
     * Tries to reload current directory
     * @return information on success
     */
    OpenResult reload();

}

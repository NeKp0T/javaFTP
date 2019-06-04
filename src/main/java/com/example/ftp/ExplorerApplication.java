package com.example.ftp;

import com.example.ftp.model.ConnectionException;
import com.example.ftp.model.DirectoryModel;
import com.example.ftp.model.DirectoryView;
import com.example.ftp.model.FileView;
import com.example.ftp.model.implementations.DirectoryModelImpl;
import com.example.ftp.server.Server;

import java.io.IOException;
import java.util.Scanner;

public class ExplorerApplication {

    public static void main(String[] args) {

        var serv = new Server("127.0.0.1", 2599);

        new Thread(() -> {
            try {
                serv.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DirectoryModel model;
        try {
            model = new DirectoryModelImpl("127.0.0.1");
        } catch (ConnectionException e) {
            e.printStackTrace();
            return;
        }

        var sc = new Scanner(System.in);

        model.openDirectoryByAbsolutePath("/");

        while (true) {
            String type = sc.next();
            switch (type) {
                case "op":
                    int ind = sc.nextInt();
                    System.out.println(model.openDirectory(ind));
                    break;
                case "get":
                    System.out.println(model.getFileContents(sc.nextInt()));
                    break;
                case "up":
                    System.out.println(model.up());
                case "reload":
                    System.out.println(model.reload());
                case "cd":
                    System.out.println(model.openSubdirectory(sc.next()));
                    break;
                case "abs":
                    System.out.println(model.openDirectoryByAbsolutePath(sc.next()));
                    break;
                case "ls":
                    for (DirectoryView d : model.getDirectories()) {
                        System.out.println(d.getName());
                    }

                    for (FileView f : model.getFiles()) {
                        System.out.println(f.getName());
                    }
                    break;
            }
        }
    }
}

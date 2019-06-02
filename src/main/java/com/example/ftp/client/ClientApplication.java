package com.example.ftp.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ClientApplication {

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("Please provide 1 argument");
//            return;
//        }

        String address = "localhost";
//        try (var scanner = new Scanner(new InputStreamReader(System.in))) {
//            address = scanner.next();
//        }

        var serv = new Server(address, 2599);

        new Thread(() -> {
            try {
                serv.startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Client client = Client.connect(address);
            System.out.println(client.listRequest("/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Client client2 = Client.connect(address);
            System.out.println(client2.listRequest("/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

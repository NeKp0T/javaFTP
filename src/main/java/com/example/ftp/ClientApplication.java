package com.example.ftp;

import com.example.ftp.client.Client;
import com.example.ftp.server.Server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ClientApplication {

    public static void main(String[] args) {
//        if (args.length != 2) {
//            System.out.println("Please provide 1 argument");
//            return;
//        }

        String address = "127.0.0.1";
//        try (var scanner = new Scanner(new InputStreamReader(System.in))) {
//            address = scanner.next();
//        }

        var serv = new Server("127.0.0.1");

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

        Client client;
        try {
            client = Client.connect(address);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var sc = new Scanner(System.in);

        while (true) {
            String type = sc.next();
            try {
                if ("list".equals(type)) {
                    System.out.println(client.listRequest(sc.next()));
                } else if ("get".equals(type)) {
                    System.out.println(client.getRequest(sc.next()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

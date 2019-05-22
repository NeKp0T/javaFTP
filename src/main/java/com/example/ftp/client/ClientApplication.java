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

        String address;
        try (var scanner = new Scanner(new InputStreamReader(System.in))) {
            address = scanner.next();
        }

        try {
            Client client = Client.connect(address);
            System.out.println(client.listRequest("/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

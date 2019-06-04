package com.example.ftp.client;

import com.example.ftp.server.Server;

import java.io.IOException;

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

        try {
            Client client = Client.connect(address);
            System.out.println("Client: got list request answer: " + client.listRequest("/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Client client = Client.connect(address);
            System.out.println("Client: got list request answer: " + client.listRequest("/kek"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

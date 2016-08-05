package com.example.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Project: ImageLoader.
 * Data: 2016/7/27.
 * Created by 8luerain.
 * Contact:<a href="mailto:8luerain@gmail.com">Contact_me_now</a>
 */
public class Server extends Thread {
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(11111);
            while (true) {
                Socket socket = serverSocket.accept();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

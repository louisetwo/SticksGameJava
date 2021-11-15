package com.company;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class ThreadedEchoServer {
    public ThreadedEchoServer(){};
    int PORT = 8000;
    ServerSocket serverSocket = null;
    Socket socket = null;
    int maxClients = 2;

    ArrayList<EchoThread> clients = new ArrayList<EchoThread>();
    GameStatus gameStatus = GameStatus.WAITING_CONNECTION;
    int turn = 0;

    public int changeTurn(){
        turn = (turn + 1) % clients.size();
        return turn;
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Waiting for client connections...");
        while (maxClients > clients.size()) {
            try {
                socket = serverSocket.accept();
                EchoThread newClient = new EchoThread(socket, clients.size(), this);
                clients.add(newClient);
                newClient.start();
                if(maxClients == clients.size()) {
                    gameStatus = GameStatus.PICKING_STICKS;
                }
                System.out.println("Game status: " + gameStatus);
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


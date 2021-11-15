package com.company;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class EchoThread extends Thread {
    protected Socket socket;
    private int clientNumber;
    private int sticks;
    private ThreadedEchoServer server;
    private int choice = -1;
    private int guess = -1;

    public EchoThread(Socket socket, int clientNumber, ThreadedEchoServer server) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        this.server = server;
        this.sticks = 3;
        this.choice = -1;
        this.guess = -1;
    }

    public void run() {
        System.out.println("Running thread");
        ObjectOutputStream output;
        ObjectInputStream input;
        boolean isClosed = false;
        String message = "";

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            System.out.println(String.format("socket established with: Client %s", clientNumber));
            System.out.println(String.format("Connections: %d", server.clients.size()));
            output.writeObject(String.format("Connection established successfully with client %s...\n", clientNumber));

            while(server.gameStatus == GameStatus.WAITING_CONNECTION) {
                Thread.sleep(1000);
            }

            output.writeObject("START");
            output.writeObject(Integer.toString(this.sticks));

            while (!isClosed) {
                do {
                    try {
                        Thread.sleep(1000);
                        this.choice = -1;
                        this.guess = -1;
                        message = (String) input.readObject();
                        System.out.println(String.format("client %s >> %s", clientNumber, message));
                        this.choice = Integer.parseInt(message);
                        output.writeObject("Mensagem recebida");

                        // checa se todos os clientes escolheram os palitos
                        if(server.clients.stream().filter(c -> c.choice == -1).findFirst().orElse(null) == null) {
                            server.gameStatus = GameStatus.GUESSING;
                        }
                        while(server.gameStatus == GameStatus.PICKING_STICKS) {
                            Thread.sleep(1000);
                        }

                        output.writeObject("Todos escolheram. Adivinhe quantos palitos há no total");

                        message = (String) input.readObject();
                        System.out.println(String.format("client %s >> %s", clientNumber, message));
                        boolean isValid = true;
                        do {
                            isValid = true;
                            for(EchoThread c : server.clients) {
                                if(c.guess == Integer.parseInt(message)) {
                                    isValid = false;
                                    break;
                                }
                            }
                            if(!isValid) {
                                output.writeObject("Valor já escolhido, escolha outro por favor");
                                message = (String) input.readObject();
                                System.out.println(String.format("client %s >> %s", clientNumber, message));
                            }
                        } while(!isValid);

                        this.guess = Integer.parseInt(message);
                        output.writeObject("Mensagem recebida");

                        // checa se todos os clientes enviaram o chute
                        if(server.clients.stream().filter(c -> c.guess == -1).findFirst().orElse(null) == null) {
                            server.gameStatus = GameStatus.CHECKING;
                        }
                        while(server.gameStatus == GameStatus.GUESSING) {
                            Thread.sleep(1000);
                        }

                        Integer totalSticks = server.clients.stream().reduce(0, (acc, c) -> acc + c.choice, Integer::sum);

                        if(this.guess == totalSticks) {
                            this.sticks--;
                            output.writeObject(String.format("Você acertou! Parabéns! Número de palitos em mãos: %d", this.sticks));
                            output.writeObject(Integer.toString(this.sticks));
                        } else {
                            output.writeObject(String.format("Você errou! Tente novamente. Número de palitos em mãos: %d", this.sticks));
                            output.writeObject(Integer.toString(this.sticks));
                        }

                        Thread.sleep(1000);

                        EchoThread winner = server.clients.stream().filter(c -> c.sticks == 0).findFirst().orElse(null);
                        if(winner == null) {
                            output.writeObject("Ninguém venceu ainda...");
                            server.gameStatus = GameStatus.PICKING_STICKS;
                        } else if(winner.clientNumber == this.clientNumber) {
                            output.writeObject("Você venceu! Parabéns!");
                            server.gameStatus = GameStatus.DONE;
                        } else {
                            output.writeObject(String.format("O client %d venceu!!", winner.clientNumber));
                            server.gameStatus = GameStatus.DONE;
                        }

                        if(server.gameStatus == GameStatus.DONE) {
                            break;
                        }

                    } catch (IOException iOException) {
                        System.err.println("err: " + iOException.toString());
                    }
                } while (true);

                isClosed = true;
                output.close();
                input.close();
                socket.close();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.toString());
        }
    }
}
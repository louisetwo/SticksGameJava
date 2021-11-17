package com.company;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public void start(String address, int port) {
        ObjectOutputStream output;
        ObjectInputStream input;
        Socket socket;
        Scanner scanner = new Scanner(System.in);
        String message = "";
        int sticks = -1;
        try {
            socket = new Socket(address, port);
            System.out.println("connecting to the server: " + address + ", port: " + port);
            System.out.println("Write: END to finish socket connection");

            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            // captura conexão estabelecida
            message = (String) input.readObject(); // Connection established successfully with client %s...
            System.out.println("Server>> " + message);

            // captura start
            message = (String) input.readObject(); // START
            System.out.println("Server start >> " + message);

            // captura numero de sticks
            message = (String) input.readObject(); // numero de sticks (só o int)
            sticks = Integer.parseInt(message);
            System.out.println("Número de palitos >> " + sticks);

            do {
                do {
                    System.out.print("Quantos palitos você quer colocar em jogo? ");
                    message = scanner.nextLine();
                } while(Integer.parseInt(message) > sticks || Integer.parseInt(message) < 0); // ||  Verifica se os palitos então entre 0 até o que o player tem na mão

                output.writeObject(message);
                output.flush();

                message = (String) input.readObject();
                System.out.println("Server>> " + message); // "Mensagem recebida"

                message = (String) input.readObject();
                System.out.println("Server>> " + message); // "Todos escolheram. Adivinhe quantos palitos há no total"

                do {
                    message = scanner.nextLine();    // Depende se o valor foi escolhido ou não
                    output.writeObject(message);
                    output.flush();
                    message = (String) input.readObject();
                    System.out.println("Server>> " + message); // "Mensagem recebida" || "Valor já escolhido, escolha outro por favor"
                } while(message.equals("Valor já escolhido, escolha outro por favor"));

                message = (String) input.readObject();
                System.out.println("Server>> " + message); // "Você acertou! Parabéns! Número de palitos em mãos: %d" || "Você errou! Tente novamente Número de palitos em mãos: %d"

                message = (String) input.readObject(); // atualiza quantidade de palitos
                sticks = Integer.parseInt(message);

                message = (String) input.readObject();
                System.out.println("Server>> " + message); // "Ninguém venceu ainda..." || "Você venceu! Parabéns!" || "O client X venceu!"

                if(message.equals("Você venceu! Parabéns!") || message.contains("O client")) {
                    break;
                }

            } while (true);

            output.close();
            input.close();
            socket.close();
            scanner.close();
        } catch (Exception e) {
            System.err.println("err: " + e.toString());
        }
    }

    public static void main(String[] args) {
        Client c = new Client();
        c.start("localhost", 8000);
    }
}
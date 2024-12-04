package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private static final String HOST = "localhost";
    private static final int PORT = 1000;

    private Socket client;
    private BufferedReader reader;
    private PrintWriter writer;

    private boolean done;

    public Client() {
        done = false;
    }

    @Override
    public void run() {
        try {
            client = new Socket(HOST, PORT);
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler handler = new InputHandler();
            new Thread(handler).start();

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
           shutdown();
        }
    }

    private void shutdown() {
        done = true;
        try {
            reader.close();
            writer.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class InputHandler implements Runnable {

        private static final String QUIT = "/quit";

        private BufferedReader reader;

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = reader.readLine();
                    if (message.equals(QUIT)) {
                        writer.println(QUIT);
                        reader.close();
                        shutdown();
                    } else {
                        writer.println(message);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}

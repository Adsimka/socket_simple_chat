package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static constants.Message.CONNECTED;
import static constants.Message.ENTER_NICKNAME;
import static constants.Message.LEFT_THE_CHAT;
import static constants.Message.NICKNAME_IS_NOT_VALID;
import static constants.Message.RENAMED_THEMSELVES;
import static constants.Message.SUCCESSFULLY_CHANGE;
public class Server implements Runnable {

    private static final int PORT = 1000;

    private List<ConnectionHandler> connections;
    private ServerSocket server;
    private ExecutorService pool;

    private boolean done;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(PORT);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();

                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);

                pool.execute(handler);
            }
        } catch (IOException ex) {
            shutdown();
        }
    }

    private void broadcast(String message) {
        for (ConnectionHandler handler : connections) {
            if (handler != null) {
                handler.sendMessage(message);
            }
        }
    }

    private void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
                pool.shutdown();
            }
            for (ConnectionHandler handler : connections) {
                handler.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    class ConnectionHandler implements Runnable {

        private static final String QUIT = "/quit";
        private static final String NICK = "/nick";

        private Socket client;
        private BufferedReader reader;
        private PrintWriter writer;

        private String nickname;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                writer = new PrintWriter(client.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer.println(ENTER_NICKNAME.getString());

                    nickname = reader.readLine();
                    broadcast(CONNECTED.getString(nickname));

                    String message;
                    while ((message = reader.readLine()) != null) {
                        if (message.startsWith(NICK)) {
                            String[] messageSplit = message.split(" ", 2);
                            if (messageSplit.length == 2) {
                                broadcast(RENAMED_THEMSELVES.getString(nickname, messageSplit[1]));
                                nickname = messageSplit[1];
                                writer.println(SUCCESSFULLY_CHANGE.getString(nickname));
                            } else {
                                writer.println("No nickname provided!");
                            }
                        } else if (message.startsWith(QUIT)) {
                            broadcast(LEFT_THE_CHAT.getString(nickname));
                            shutdown();
                        } else {
                            broadcast(nickname + ": " + message);
                        }
                    }
            } catch (IOException e) {
                shutdown();
            }
        }

        protected void sendMessage(String message) {
            writer.println(message);
        }

        private String getValidName(String nickname) {
            if (Objects.nonNull(nickname) && nickname.isBlank()) {
                return nickname;
            }
            throw new IllegalArgumentException(NICKNAME_IS_NOT_VALID.getString());
        }

        private void shutdown() {
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
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}

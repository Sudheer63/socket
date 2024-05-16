import java.io.*;
import java.net.*;
import java.util.*;

public class Serversidechat {
    
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Set<String> clientNames = new HashSet<>();

    public static void main(String[] args) {
        int PORT=Integer.parseInt(args[0]);
        System.out.println("Chat server started...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read client name
                clientName = in.readLine();
                synchronized (clientWriters) {
                    clientWriters.add(out);
                    clientNames.add(clientName);
                    broadcastMemberList();
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                    clientNames.remove(clientName);
                    broadcastMemberList();
                }
                if (out != null) {
                    out.close();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }

        private void broadcastMemberList() {
            synchronized (clientWriters) {
                String members = "MEMBER: " + String.join(",", clientNames);
                for (PrintWriter writer : clientWriters) {
                    writer.println(members);
                }
            }
        }
    }
}

import javax.net.ssl.*;
import java.security.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class SMPServer {
    private static final int PORT = 12345;
    private static Map<String, String> messageStore = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        try {
            char[] keystorePassword = "your_keystore_password".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("server_keystore.jks");
            keyStore.load(fis, keystorePassword);
            fis.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, keystorePassword);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT);

            System.out.println("SMP Server started with SSL on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (Exception e) {
            System.out.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private SSLSocket socket;
        private BufferedReader input;
        private PrintWriter output;

        public ClientHandler(SSLSocket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

                output.println("Welcome to SMP Server. Please log in.");

                String message;
                while ((message = input.readLine()) != null) {
                    System.out.println("Received: " + message);
                    handleRequest(message);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void handleRequest(String request) {
            String[] parts = request.split(":", 3);
            String command = parts[0];

            switch (command) {
                case "LOGON":
                    output.println("LOGON_SUCCESS");
                    break;
                case "UPLOAD":
                    if (parts.length == 3) {
                        String messageID = parts[1];
                        String messageText = parts[2];
                        messageStore.put(messageID, messageText);
                        output.println("UPLOAD_SUCCESS");
                    } else {
                        output.println("UPLOAD_FAILURE");
                    }
                    break;
                case "DOWNLOAD":
                    if (parts.length == 2) {
                        String messageID = parts[1];
                        if (messageStore.containsKey(messageID)) {
                            output.println("MESSAGE:" + messageID + ":" + messageStore.get(messageID));
                        } else {
                            output.println("ERROR:Message_not_found");
                        }
                    }
                    break;
                case "DOWNLOAD_ALL_MESSAGES":
                    if (messageStore.isEmpty()) {
                        output.println("No messages stored.");
                    } else {
                        for (Map.Entry<String, String> entry : messageStore.entrySet()) {
                            output.println("MESSAGE:" + entry.getKey() + ":" + entry.getValue());
                        }
                    }
                    output.println("END");
                    break;
                case "LOGOFF":
                    output.println("LOGOFF_SUCCESS");
                    break;
                default:
                    output.println("ERROR:Invalid_Command");
            }
        }
    }
}
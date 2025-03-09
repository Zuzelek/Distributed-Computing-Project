import javax.net.ssl.*;
import java.security.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SMPClient {
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;

    public SMPClient(String serverAddress, int port) {
        try {
            char[] truststorePassword = "your_truststore_password".toCharArray();
            KeyStore trustStore = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream("client_truststore.jks");
            trustStore.load(fis, truststorePassword);
            fis.close();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory sf = sslContext.getSocketFactory();
            socket = (SSLSocket) sf.createSocket(serverAddress, port);

            socket.startHandshake();

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);

            System.out.println("Connected securely to SMP Server at " + serverAddress + ":" + port);

            runClient();
        } catch (Exception e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void runClient() {
        try {
            // Display welcome message from server
            String serverMessage = in.readLine();
            System.out.println("Server: " + serverMessage);

            while (true) {
                System.out.print("Enter command (LOGON, UPLOAD, DOWNLOAD, DOWNLOAD_ALL_MESSAGES, LOGOFF): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("LOGOFF")) {
                    sendMessage("LOGOFF");
                    String response = in.readLine();
                    System.out.println("Server Response: " + response);
                    break;
                } else if (input.startsWith("UPLOAD")) {
                    System.out.print("Enter message ID: ");
                    String messageID = scanner.nextLine();

                    System.out.print("Enter message text: ");
                    String messageText = scanner.nextLine();

                    sendMessage("UPLOAD:" + messageID + ":" + messageText);
                } else if (input.startsWith("DOWNLOAD")) {
                    System.out.print("Enter message ID: ");
                    String messageID = scanner.nextLine();

                    sendMessage("DOWNLOAD:" + messageID);
                } else if (input.equals("DOWNLOAD_ALL_MESSAGES")) {
                    sendMessage("DOWNLOAD_ALL_MESSAGES");

                    String response;
                    while (!(response = in.readLine()).equals("END")) {
                        System.out.println("Server Response: " + response);
                    }
                    continue; // Skip the regular response handling
                } else if (input.startsWith("LOGON")) {
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();

                    sendMessage("LOGON:" + username);
                } else {
                    sendMessage(input);
                }

                // Handle regular responses
                String response = in.readLine();
                System.out.println("Server Response: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error in communication: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void sendMessage(String message) {
        out.println(message);
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 12345;

        if (args.length >= 1) {
            serverAddress = args[0];
        }

        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default: 12345");
            }
        }

        new SMPClient(serverAddress, serverPort);
    }
}
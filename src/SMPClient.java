import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SMPClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner scanner;

    public SMPClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scanner = new Scanner(System.in);

            System.out.println("Connected to SMP Server at " + serverAddress + ":" + port);

            runClient();

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void runClient() {
        while (true) {
            System.out.print("Enter command (LOGON, UPLOAD, DOWNLOAD, LOGOFF): ");
            String command = scanner.nextLine();

            if (command.equalsIgnoreCase("LOGOFF")) {
                sendMessage("LOGOFF");
                break;
            }

            sendMessage(command);
            receiveResponse();
        }
        closeConnection();
    }

    private void sendMessage(String message) {
        out.println(message);
    }

    private void receiveResponse() {
        try {
            String response = in.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new SMPClient("localhost", 12345);
    }
}

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SMPClient {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;

    public SMPClient(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);

            System.out.println("Connected to SMP Server at " + serverAddress + ":" + serverPort);
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void start() {
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Log in");
            System.out.println("2. Upload Message");
            System.out.println("3. Download Specific Message");
            System.out.println("4. Download All Messages");
            System.out.println("5. Logoff");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    logon();
                    break;
                case 2:
                    uploadMessage();
                    break;
                case 3:
                    downloadSpecificMessage();
                    break;
                case 4:
                    downloadAllMessages();
                    break;
                case 5:
                    logoff();
                    break;
                case 6:
                    closeConnection();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void logon() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String logonMessage = "LOGON:" + username + ":" + password;
        output.println(logonMessage);

        try {
            String response = input.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void uploadMessage() {
        System.out.print("Enter message ID: ");
        String messageID = scanner.nextLine();
        System.out.print("Enter message text: ");
        String messageText = scanner.nextLine();

        String uploadMessage = "UPLOAD:" + messageID + ":" + messageText;
        output.println(uploadMessage);

        try {
            String response = input.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void downloadSpecificMessage() {
        System.out.print("Enter message ID to download: ");
        String messageID = scanner.nextLine();

        String downloadMessage = "DOWNLOAD:" + messageID;
        output.println(downloadMessage);

        try {
            String response = input.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void downloadAllMessages() {
        output.println("DOWNLOAD_ALL_MESSAGES");

        try {
            String response;
            while (!(response = input.readLine()).equals("END")) { // Server will send "END" when all messages are sent
                System.out.println("Server: " + response);
            }
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void logoff() {
        output.println("LOGOFF");

        try {
            String response = input.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.out.println("Error reading server response: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            System.out.println("Closing connection...");
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SMPClient client = new SMPClient("localhost", 12345);
        client.start();
    }
}

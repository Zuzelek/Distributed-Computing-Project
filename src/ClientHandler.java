import java.io.*;
import java.net.*;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            out.println("Welcome to SMP Server. Please log in.");

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);

                if (message.startsWith("LOGON:")) {
                    out.println("LOGON_SUCCESS");
                } else if (message.equals("LOGOFF")) {
                    out.println("LOGOFF_SUCCESS");
                    break;
                } else {
                    out.println("ERROR: Unknown command");
                }
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

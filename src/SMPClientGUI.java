import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SMPClientGUI {
    private JFrame frame;
    private JTextField messageIDField;
    private JTextField messageTextField;
    private JTextArea responseArea;
    private PrintWriter output;
    private BufferedReader input;
    private Socket socket;
    private boolean isLoggedIn = false;

    public SMPClientGUI() {
        initializeUI();
        connectToServer();
    }

    private void initializeUI() {
        frame = new JFrame("SMP Client");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel messageIDLabel = new JLabel("Message ID:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        frame.add(messageIDLabel, gbc);

        messageIDField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(messageIDField, gbc);

        JLabel messageTextLabel = new JLabel("Message Text:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(messageTextLabel, gbc);

        messageTextField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        frame.add(messageTextField, gbc);

        responseArea = new JTextArea(10, 40);
        responseArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(responseArea);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(scrollPane, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        String[] buttonLabels = {"Log In", "Upload", "Download", "Download All", "Log Off", "Exit"};
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.addActionListener(new ButtonClickListener());
            buttonPanel.add(button);
        }

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 3;
        frame.add(buttonPanel, gbc);

        frame.setVisible(true);
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            logMessage("Connected to server.");
        } catch (IOException e) {
            logMessage("Error connecting to server: " + e.getMessage());
        }
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            responseArea.append(message + "\n");
            System.out.println(message);
        });
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            switch (command) {
                case "Log In":
                    sendMessageToServer("LOGON");
                    isLoggedIn = true; // Mark user as logged in
                    break;
                case "Upload":
                    if (validateLogin()) uploadMessage();
                    break;
                case "Download":
                    if (validateLogin()) downloadSpecificMessage();
                    break;
                case "Download All":
                    if (validateLogin()) sendMessageToServer("DOWNLOAD_ALL_MESSAGES");
                    break;
                case "Log Off":
                    if (validateLogin()) {
                        sendMessageToServer("LOGOFF");
                        isLoggedIn = false; // Mark user as logged out
                    }
                    break;
                case "Exit":
                    closeConnection();
                    break;
            }
        }
    }

    private boolean validateLogin() {
        if (!isLoggedIn) {
            logMessage("You must log in first!");
            return false;
        }
        return true;
    }

    private void uploadMessage() {
        String messageID = messageIDField.getText();
        String messageText = messageTextField.getText();
        if (messageID.isEmpty() || messageText.isEmpty()) {
            logMessage("Message ID and Text cannot be empty.");
            return;
        }
        sendMessageToServer("UPLOAD:" + messageID + ":" + messageText);
    }

    private void downloadSpecificMessage() {
        String messageID = messageIDField.getText();
        if (messageID.isEmpty()) {
            logMessage("Message ID cannot be empty.");
            return;
        }
        sendMessageToServer("DOWNLOAD:" + messageID);
    }

    private void closeConnection() {
        sendMessageToServer("LOGOFF");
        try {
            if (socket != null) {
                socket.close();
            }
            logMessage("Connection closed.");
            System.exit(0);
        } catch (IOException e) {
            logMessage("Error closing connection: " + e.getMessage());
        }
    }

    private void sendMessageToServer(String message) {
        new Thread(() -> {
            try {
                output.println(message);
                String response;
                while ((response = input.readLine()) != null) {
                    if (response.equals("END")) break;
                    logMessage("Server: " + response);
                }
            } catch (IOException e) {
                logMessage("Error reading server response: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SMPClientGUI::new);
    }
}

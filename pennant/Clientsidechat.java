import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;

public class Clientsidechat extends Frame {
    private static final String SERVER_ADDRESS = "localhost";
    public int port;
    private TextArea chatArea;
    private TextField inputField;
    private List membersList;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String clientName;

    public Clientsidechat(String name,int port) {
        super("Chat Client - " + name);
        this.clientName = name;
        this.port=port;

        Font font = new Font("Arial", Font.PLAIN, 16);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        Panel sendPanel = new Panel(new BorderLayout());
        sendPanel.add(new Label("Send Messages:"), BorderLayout.NORTH);
        inputField = new TextField();
        inputField.setFont(font);
        sendPanel.add(inputField, BorderLayout.CENTER);
        Button sendButton = new Button("Send");
        sendButton.setFont(font);
        sendPanel.add(sendButton, BorderLayout.EAST);

        Panel membersPanel = new Panel(new BorderLayout());
        membersPanel.add(new Label("Connected Members:"), BorderLayout.NORTH);
        membersList = new List();
        membersList.setFont(font);
        membersPanel.add(membersList, BorderLayout.CENTER);

        Panel chatPanel = new Panel(new BorderLayout());
        chatPanel.add(new Label("Received Messages:"), BorderLayout.NORTH);
        chatArea = new TextArea();
        chatArea.setFont(font);
        chatArea.setEditable(false);
        chatPanel.add(chatArea, BorderLayout.CENTER);

        // Configure layout constraints
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.3; // Width for sendPanel
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(sendPanel, gbc);

        gbc.weightx = 0.1; // Width for padding
        gbc.gridx = 1;
        add(new Panel(), gbc); // Padding

        gbc.weightx = 0.6; // Width for membersPanel
        gbc.gridx = 2;
        gbc.gridheight = 2; // Span two rows for membersPanel
        add(membersPanel, gbc);

        gbc.weightx = 0.3; // Width for chatPanel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 1; // Single row for chatPanel
        add(chatPanel, gbc);

        ActionListener sendListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.trim().isEmpty()) {
                    out.println(clientName + ": " + message);
                    inputField.setText("");
                }
            }
        };
        inputField.addActionListener(sendListener);
        sendButton.addActionListener(sendListener);

        setSize(800, 600);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_ADDRESS, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(clientName);

            Executors.newSingleThreadExecutor().execute(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("MEMBER: ")) {
                            updateMembersList(response.substring(8));
                        } else {
                            chatArea.append(response + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMembersList(String memberNames) {
        membersList.removeAll();
        for (String name : memberNames.split(",")) {
            membersList.add(name);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Clientsidechat <name>");
            System.exit(1);
        }
        String name=args[0];
        int SERVER_PORT = Integer.parseInt(args[1]);
        new Clientsidechat(name,SERVER_PORT);
    }
}

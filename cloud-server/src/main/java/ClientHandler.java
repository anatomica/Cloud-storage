import Json.*;
import java.io.*;
import java.net.Socket;
import java.sql.*;

class ClientHandler {

    private MyServer myServer;
    private String clientName;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private static Connection conn;
    private static Statement stmt;
    private FileOutputStream fos;

    ClientHandler(Socket socket, MyServer myServer) {
        try {
            this.socket = socket;
            this.myServer = myServer;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
//                    while (true) {
//                        if (authentication()) {
//                            break;
//                        }
//                    }
                    listenClient();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания подключения к клиенту!", e);
        }
    }

    private void listenClient() throws IOException, SQLException {
        while (true) {
            String clientMessage = in.readUTF();
            Message m = Message.fromJson(clientMessage);
            switch (m.command) {
                case RECEIVE_FILE:
                    break;
                case SEND_FILE:
                    while (true) {
                        int length = in.readInt();
                        // long length = m.sendFile.sizeFile;
                        if (length > 0) {
                            byte[] message = new byte[length];
                            in.readFully(message, 0, message.length);
                            fos = new FileOutputStream("NewAnswer.txt", true);
                            fos.write(message);
                        }
                        break;
                    }
                    break;
                case RENAME_FILE:
                    break;
                case DELETE_FILE:
                    return;
            }
        }
    }

    private boolean authentication() throws IOException, SQLException {
        String clientMessage = in.readUTF();
        return true;
    }

    void sendMessage(String message)  {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            System.err.println("Ошибка отправки сообщения пользователю: " + clientName + " : " + message);
            e.printStackTrace();
        }
    }

    String getClientName() {
        return clientName;
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite::resource:LoginData.db");
        stmt = conn.createStatement();
    }

    private static void disconect() throws SQLException {
        stmt.close();
        conn.close();
    }

    private void closeConnection() {
        // myServer.unsubscribe(this);
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket!");
            e.printStackTrace();
        }
    }
}

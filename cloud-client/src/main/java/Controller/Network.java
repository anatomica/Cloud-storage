package Controller;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class Network implements Closeable {

    private final String serverAddress;
    private final int port;
    private final FileService fileService;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    Network(String serverAddress, int port, FileService fileService) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.fileService = fileService;
        initNetworkState(serverAddress, port);
    }

    private void initNetworkState(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());

        Thread readServerThread = new Thread(this::readMessagesFromServer);
        readServerThread.setDaemon(true);
        readServerThread.start();
    }

    private void readMessagesFromServer() {
        while (true) {
            try {
                String message = inputStream.readUTF();
                Platform.runLater(() -> {
                    try {
                        fileService.processRetrievedFile(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                System.out.println("Соединение с сервером было разорвано!");
                break;
            }
        }
    }

    void send (String message) {
        try {
            if (outputStream == null) {
                initNetworkState(serverAddress, port);
            }
            outputStream.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send message: " + message);
        }
    }

    void send (File file) {
        try {
            if (outputStream == null) {
                initNetworkState(serverAddress, port);
            }
            outputStream.writeInt(Files.readAllBytes(file.toPath()).length);
            outputStream.write(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send message: " + file.getName());
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
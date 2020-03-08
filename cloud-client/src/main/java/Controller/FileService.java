package Controller;

import Json.*;
import java.io.*;
import java.util.Properties;

class FileService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";
    private String hostAddress;
    private int hostPort;

    private Controller controller;
    private Network network;

    FileService(Controller controller) throws IOException {
        this.controller = controller;
        initialize();
    }

    private void initialize() {
        readProperties();
        startConnectionToServer();
    }

    private void readProperties() {
        Properties serverProperties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            serverProperties.load(inputStream);
            hostAddress = serverProperties.getProperty(HOST_ADDRESS_PROP);
            hostPort = Integer.parseInt(serverProperties.getProperty(HOST_PORT_PROP));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.properties file", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value", e);
        }
    }

    private void startConnectionToServer() {
        try {
            this.network = new Network(hostAddress, hostPort, this);
        } catch (IOException e) {
            throw new ServerConnectionException("Failed to connect to server", e);
        }
    }

    void sendFile(File file) {
        Message msg = buildMessage(file);
        network.send(msg.toJson());
        network.send(file);
    }

    private Message buildMessage(File file) {
        SendFile msg = new SendFile();
        msg.nameFile = file.getName();
        msg.pathFile = file.getPath();
        msg.sizeFile = file.length();
        return Message.sendFile(msg);
    }

    void processRetrievedFile(String message) throws IOException {
    }

    void close() throws IOException {
        network.close();
        System.exit(0);
    }
}
package Controller;

import File.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
        controller.refreshFilesList();
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
            this.network = new Network(hostAddress, hostPort, controller,this);
        } catch (IOException e) {
            throw new ServerConnectionException("Failed to connect to server", e);
        }
    }

    void receiveFile(String filename) {
        network.sendMsg(new FileRequest(filename));
    }

    void sendFile(Path path) throws IOException, InterruptedException {
        network.sendMsg(new FileMessage(path));
        TimeUnit.SECONDS.sleep(1);
        controller.refreshFilesList();
    }

    void close() throws IOException {
        network.close();
        System.exit(0);
    }
}
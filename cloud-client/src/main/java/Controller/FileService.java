package Controller;

import Handlers.*;
import Protocol.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class FileService {

    private static final String HOST_ADDRESS_PROP = "server.address";
    private static final String HOST_PORT_PROP = "server.port";
    public static String hostAddress;
    public static int hostPort;

    private Controller controller;
    private Callback authOkCallback;

    FileService(Controller controller, Callback authOkCallback) {
        this.controller = controller;
        this.authOkCallback = authOkCallback;
        initialize();
    }

    private void initialize() {
        readProperties();
        authOkCallback = () -> {
            controller.imageBox.setVisible(false);
            controller.authPanel.setVisible(false);
            controller.workPanel.setVisible(true);
            TimeUnit.MILLISECONDS.sleep(100);
            controller.refreshFilesList();
            System.out.println("Вход выполнен!");
        };
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

    public void startConnectionToServer() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(authOkCallback, networkStarter)).start();
        networkStarter.await();
    }

    void receiveFile(String filename) throws IOException {
        ProtocolFiles.receiveFile(Paths.get(filename), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Команда на получение передана");
                TimeUnit.MILLISECONDS.sleep(200);
                Thread waitReceive = new Thread(this::refreshList);
                waitReceive.setDaemon(true);
                waitReceive.start();
            }
        });
    }

    void sendFile(Path path) throws IOException {
        ProtocolFileSender.sendFile(Paths.get("client_storage/" + path.getFileName()), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан");
                TimeUnit.MILLISECONDS.sleep(200);
                controller.refreshFilesList();
            }
        });
    }

    public void deleteLocalFiles(String filename) throws IOException {
        Files.delete(Paths.get("client_storage/" + filename).toAbsolutePath());
        controller.refreshFilesList();
    }

    public void deleteCloudFiles(String filename) {
        ProtocolFiles.deleteFile(Paths.get(filename), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Команда на удаление передана!");
                TimeUnit.MILLISECONDS.sleep(200);
                Thread waitReceive = new Thread(this::refreshList);
                waitReceive.setDaemon(true);
                waitReceive.start();
            }
        });
    }

    public void renameFiles(String oldFilename, String newFileName) throws IOException {
        Files.move(Paths.get("client_storage/" + oldFilename).toAbsolutePath(), Paths.get("client_storage/" + newFileName).toAbsolutePath());
        controller.refreshFilesList();
    }

    public void refreshList() {
        while (true) {
            try {
                if (ProtocolHandler.checkReceiveFile()) {
                    TimeUnit.MILLISECONDS.sleep(500);
                    controller.refreshFilesList();
                    System.out.println("Обновлено!");
                    break;
                }
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void close() throws IOException {
        Network.getInstance().stop();
        System.exit(0);
    }
}
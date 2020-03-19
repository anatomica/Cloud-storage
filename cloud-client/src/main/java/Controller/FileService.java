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

    private Controller controller;

    FileService(Controller controller) throws InterruptedException, IOException {
        this.controller = controller;
        initialize();
    }

    private void initialize() throws InterruptedException {
        readProperties();
        startConnectionToServer();
        Thread waitLogin = new Thread(this::autoChangeView);
        waitLogin.setDaemon(true);
        waitLogin.start();
    }

    private void readProperties() {
        Properties serverProperties = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
            serverProperties.load(inputStream);
            serverProperties.getProperty(HOST_ADDRESS_PROP);
            Integer.parseInt(serverProperties.getProperty(HOST_PORT_PROP));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read application.properties file", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value", e);
        }
    }

    private void startConnectionToServer() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
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

    public void refreshList() {
        while (true) {
            if (ProtocolHandler.checkReceiveFile()) {
                controller.refreshFilesList();
                System.out.println("Обновлено!");
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void autoChangeView() {
        while (true) {
            try {
                if (AuthHandler.checkLogin().equals("1")) {
                    controller.imageBox.setVisible(false);
                    controller.authPanel.setVisible(false);
                    controller.workPanel.setVisible(true);
                    TimeUnit.MILLISECONDS.sleep(100);
                    controller.refreshFilesList();
                    System.out.println("Вход выполнен!");
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
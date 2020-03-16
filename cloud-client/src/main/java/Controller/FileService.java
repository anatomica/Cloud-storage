package Controller;

import Handlers.*;
import Protocol.*;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
// import org.apache.commons.lang3.exception.ExceptionUtils;
import javax.swing.*;
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
    private String hostAddress;
    private int hostPort;

    private Controller controller;

    FileService(Controller controller) throws InterruptedException, IOException {
        this.controller = controller;
        initialize();
    }

    private void initialize() throws InterruptedException {
        readProperties();
        startConnectionToServer();
        controller.refreshFilesList();
        Thread waitLogin = new Thread(this::autoChangeView);
        waitLogin.setDaemon(true);
        waitLogin.start();
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

    private void startConnectionToServer() throws InterruptedException {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();
    }

    void receiveFile(String filename) throws IOException {
        ProtocolFileReceive.receiveFile(Paths.get(filename), Network.getInstance().getCurrentChannel(), future -> {
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

    public void deleteFile(String filename, String storage) throws IOException {
        Files.delete(Paths.get(storage + filename).toAbsolutePath());
        controller.refreshFilesList();
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
            if (AuthHandler.checkLogin().equals("1")) {
                controller.imageBox.setVisible(false);
                controller.authPanel.setVisible(false);
                controller.workPanel.setVisible(true);
                System.out.println("Вход выполнен!");
                break;
            } try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void showError () {
        JOptionPane.showMessageDialog(null, "Вы ввели неверное имя пользователя или пароль!");
    }

    void close() throws IOException {
        Network.getInstance().stop();
        System.exit(0);
    }
}
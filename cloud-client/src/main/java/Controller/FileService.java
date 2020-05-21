package Controller;

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
    private Callback refreshCallback;

    FileService(Controller controller, Callback authOkCallback, Callback refreshCallback) {
        this.controller = controller;
        this.authOkCallback = authOkCallback;
        this.refreshCallback = refreshCallback;
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
        refreshCallback = () -> {
            controller.refreshFilesList();
            System.out.println("Обновлено!");
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
        new Thread(() -> Network.getInstance().start(authOkCallback, refreshCallback, networkStarter)).start();
        networkStarter.await();
    }

    void receiveFile(String filename) throws IOException {
        ProtocolFiles.receiveFile(Paths.get(filename), Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
            }
            if (future.isSuccess()) {
                System.out.println("Команда на получение передана");
            }
        });
    }

    void sendFile(Path path) throws IOException {
        ProtocolFiles.sendFile(Paths.get("client_storage/" + path.getFileName()), Network.getInstance().getCurrentChannel(), future -> {
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
                TimeUnit.MICROSECONDS.sleep(300);
                refreshCallback.callBack();
            }
        });
    }

    public void renameFiles(String oldFilename, String newFileName) throws IOException {
        Files.move(Paths.get("client_storage/" + oldFilename).toAbsolutePath(), Paths.get("client_storage/" + newFileName).toAbsolutePath());
        controller.refreshFilesList();
    }

    void close() throws IOException {
        Network.getInstance().stop();
        System.exit(0);
    }
}
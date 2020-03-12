package Controller;

import File.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Network2 implements Closeable {

    private final String serverAddress;
    private final int port;
    private Controller controller;
    private FileService fileService;

    private Socket socket;
    private ObjectDecoderInputStream inputStream;
    private ObjectEncoderOutputStream outputStream;

    Network2(String serverAddress, int port, Controller controller, FileService fileService) throws IOException {
        this.serverAddress = serverAddress;
        this.port = port;
        this.controller = controller;
        this.fileService = fileService;
        initNetworkState(serverAddress, port);
    }

    private void initNetworkState(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.inputStream = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        this.outputStream = new ObjectEncoderOutputStream(socket.getOutputStream());

        Thread readServerThread = new Thread(this::readObjectFromServer);
        readServerThread.setDaemon(true);
        readServerThread.start();
    }

    private void readObjectFromServer() {
        while (true) {
            try {
                Object obj = inputStream.readObject();
                if (obj instanceof FileMessage) {
                    FileMessage fm = (FileMessage) obj;
                    Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                    controller.refreshFilesList();
                }
            } catch (Exception e) {
                System.out.println("Соединение с сервером было разорвано!");
                break;
            }
        }
    }

    public void sendMsg(AbstractMessage msg) {
        try {
            outputStream.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
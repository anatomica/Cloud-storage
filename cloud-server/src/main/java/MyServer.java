import auth.AuthService;
import auth.BaseAuthService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

class MyServer {

    private static final int PORT = 8190;
    private final AuthService authService = new BaseAuthService();
    private List<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket = null;

    MyServer() {
        System.out.println("Сервер запущен!");
        try {
            serverSocket = new ServerSocket(PORT);
            authService.start();
            while (true) {
                System.out.println("Ожидание подключения клиентов ...");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключен!");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            System.err.println("Ошибка в работе сервера. Причина: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdownServer();
        }
    }

    private void shutdownServer() {
        try {
            authService.stop();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

    AuthService getAuthService() {
        return authService;
    }

}

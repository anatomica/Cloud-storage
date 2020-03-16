package auth;

import Json.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.IOException;
import java.sql.*;

public class BaseAuthService implements AuthService {

    private static Connection conn;
    private static Statement stmt;

    public static boolean authentication(String clientMessage, Channel channel) throws IOException, SQLException {
        Message message = Message.fromJson(clientMessage);
        if (message.command == Command.AUTH_MESSAGE) {
            AuthMessage authMessage = message.authMessage;
            String login = authMessage.login;
            String password = authMessage.password;
            String nick = getNickByLoginPass(login, password);
            disconect();
            ByteBuf auth = ByteBufAllocator.DEFAULT.directBuffer(1);
            if (nick == null) {
                auth.writeByte((byte) 2);
                channel.writeAndFlush(auth);
                return false;
            } else {
                auth.writeByte((byte) 3);
                channel.writeAndFlush(auth);
            }
        }
        return true;
    }

    public static String getNickByLoginPass(String login, String pass) throws SQLException {

        try {
            connection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        ResultSet rs = stmt.executeQuery("select * from LoginData");
        while (rs.next()) {
            ResultSetMetaData dataInBase = rs.getMetaData();
            for (int i = 1; i <= dataInBase.getColumnCount(); i++) {
                if (rs.getString("Login").equals(login) && rs.getString("Pass").equals(pass)) {
                    return rs.getString("Nick");
                }
            }
        }
        return null;
    }

    private static void connection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite::resource:LoginData.db");
        stmt = conn.createStatement();
    }

    public static void disconect() throws SQLException {
        stmt.close();
        conn.close();
    }

    @Override
    public void start() {
        System.out.println("Сервис авторизации запущен!");
    }

    @Override
    public void stop() {
        System.out.println("Сервис автоизации остановлен!");
    }
}

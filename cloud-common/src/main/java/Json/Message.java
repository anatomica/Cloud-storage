package Json;

import File.FileAbout;
import com.google.gson.Gson;
import java.util.List;

public class Message {

    public FilesListMessage filesListMessage;
    public AuthMessage authMessage;
    public SendFile sendFile;
    public Command command;

    public String toJson() {
        return new Gson().toJson(this);
    }

    private static Message create(Command cmd) {
        Message m = new Message();
        m.command = cmd;
        return m;
    }

    public static FilesListMessage createFilesList(List<FileAbout> files, String from) {
        Message m = create(Command.FILES_LIST);
        FilesListMessage msg = new FilesListMessage();
        msg.files = files;
        msg.from = from;
        return msg;
    }

    public static Message sendFile(SendFile msg) {
        Message m = create(Command.SEND_FILE);
        m.sendFile = msg;
        return m;
    }

    public static Message createAuth(AuthMessage msg) {
        Message m = create(Command.AUTH_MESSAGE);
        m.authMessage = msg;
        return m;
    }

//    public static Message createRegister(RegisterMessage msg) {
//        Message m = create(Command.REGISTER_MESSAGE);
//        m.registerMessage = msg;
//        return m;
//    }

    public static Message fromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }
}

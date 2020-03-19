package Json;
import File.FileAbout;
import com.google.gson.Gson;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

public class FilesListMessage {

    public List<FileAbout> files;
    public String from;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static FilesListMessage fromJson(String json) {
        return new Gson().fromJson(json, FilesListMessage.class);
    }
}

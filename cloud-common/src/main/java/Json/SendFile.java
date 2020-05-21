package Json;
import com.google.gson.Gson;

public class SendFile {

    public String nameFile;
    public String pathFile;
    public long sizeFile;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static SendFile fromJson(String json) {
        return new Gson().fromJson(json, SendFile.class);
    }
}

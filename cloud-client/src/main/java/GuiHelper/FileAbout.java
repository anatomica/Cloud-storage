package GuiHelper;

import java.io.File;
import java.io.Serializable;

public class FileAbout implements Serializable {

    private File file;
    private String name;
    private long size;

    public FileAbout(File file) {
        this.file = file;
        this.name = file.getName();
        this.size = file.length();
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
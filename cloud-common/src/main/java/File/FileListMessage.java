package File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class FileListMessage extends AbstractMessage {

    private List<FileAbout> fileList;

    public List<FileAbout> getFilesList() {
        return fileList;
    }

    public FileListMessage(Path path) throws IOException {
        fileList = Files.list(path).map(Path::toFile).map(FileAbout::new).collect(Collectors.toList());
    }
}

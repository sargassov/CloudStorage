import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Command {

    private String filename;
    private byte[] data;
    private long size;

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public long getSize() {
        return size;
    }


    FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        size = Files.size(path);
        data = Files.readAllBytes(path);
    }

    @Override
    public CommandName getCommandList() {
        return CommandName.FILE_MESSAGE;
    }
}

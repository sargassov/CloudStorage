import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ListResponce extends Command {

    private ArrayList<String> serverFileList;

    public ListResponce() {
    }

    public ListResponce(ArrayList<String> serverFileList) {
        this.serverFileList = serverFileList;
    }

    public ListResponce(Path path) throws IOException {
        serverFileList = (ArrayList<String>) Files.list(path)
                .map(f->f.getFileName().toString())
                .collect(Collectors.toList());

    }

    public ArrayList<String> getServerFileList() {
        return serverFileList;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.LIST_RESPONCE;
    }
}

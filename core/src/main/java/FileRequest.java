public class FileRequest extends Command {

    private final String fileName;

    public String getFileName() {
        return fileName;
    }

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public CommandName getCommandList() {
        return CommandName.FILE_REQUEST;
    }
}

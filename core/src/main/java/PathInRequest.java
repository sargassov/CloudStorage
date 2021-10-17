public class PathInRequest extends Command {

    private final String directory;

    public PathInRequest(String directory){
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.PATH_IN_REQUEST;
    }
}

public class PathInRequest extends Command {

    private final String directory;

    public PathInRequest(String directory){
        this.directory = directory;
    }

    public String getDirectory() {
        return directory;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.PATH_IN_REQUEST;
    }
}

public class DeleteRequest extends Command {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public DeleteRequest(String filename) {
        this.filename = filename;
    }

    @Override
    public CommandName getCommandName() {
        return CommandName.DELETE_REQUEST;
    }
}

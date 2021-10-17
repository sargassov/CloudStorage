public class PathResponce extends Command{

    private String path;

    public PathResponce(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public CommandName getCommandList() {
        return CommandName.PATH_RESPONCE;
    }
}

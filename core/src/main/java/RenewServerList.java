import java.util.ArrayList;

public class RenewServerList extends RandomMessage {

    private ArrayList<String> serverFileList;

    public RenewServerList() {
    }

    public RenewServerList(ArrayList<String> serverFileList) {
        this.serverFileList = serverFileList;
    }

    public ArrayList<String> getServerFileList() {
        return serverFileList;
    }

}

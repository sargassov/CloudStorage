public class DownloadRequest extends RandomMessage {

    private String filename;

    public String getFilename() {
        return filename;
    }

    public DownloadRequest(String filename) {
        this.filename = filename;
    }

}

public class DeleteRequest extends RandomMessage {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public DeleteRequest(String filename) {
        this.filename = filename;
    }

}

package pro.antonshu.network.message;

public class FileRequest extends Message {

    private String filename;
    private String owner;

    public FileRequest(String filename) {
        this.filename = filename;
    }

    public FileRequest(String filename, String owner) {
        this.filename = filename;
        this.owner = owner;
    }

    public String getFilename() {
        return filename;
    }

    public String getOwner() {
        return owner;
    }
}

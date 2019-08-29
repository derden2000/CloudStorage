package pro.antonshu.network.message;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Message {

    private String filename;
    private boolean isParted;
    private int totalParts;
    private int currentPart;
    private byte[] data;
    private String owner;

    public FileMessage(Path path) throws IOException {
        this.filename = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
    }

    public FileMessage(Path path, String owner) {
        this.filename = path.getFileName().toString();;
        this.owner = owner;
    }

    public FileMessage(String filename, int currentPart, int totalParts, byte[] data) {
        this.filename = filename;
        this.totalParts = totalParts;
        this.currentPart = currentPart;
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public boolean getIsParted() {
        return isParted;
    }

    public int getTotalParts() {
        return totalParts;
    }

    public int getCurrentPart() {
        return currentPart;
    }

    public byte[] getData() {
        return data;
    }

    public String getOwner() {
        return owner;
    }
}

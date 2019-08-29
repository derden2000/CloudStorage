package pro.antonshu.network.message;

import java.util.Map;

public class CommandMessage extends Message {

   private static final byte EMPTY = -1;
   private static final byte REQUEST_FILE_LIST = 127;
   private static final byte CREATE_FILE = 126;
   private static final byte DELETE_FILE = 125;
   private static final byte CREATE_DIR = 124;
   private static final byte DELETE_DIR = 123;

/*
    Поле Boolean указывает на принадлежность к папке. Значение False говорит, что это файл.
    Планирую реализовать перемещения по папкам в интерфейсе и создание/удаление файлов.
*/
    private Map<String, Boolean> fileList;

    private String commandName;

    private String owner;

    public CommandMessage(String commandName) {
        this.commandName = commandName;
    }

    public CommandMessage(String commandName, String owner) {
        this.commandName = commandName;
        this.owner = owner;
    }

    public Map<String, Boolean> getFileList() {
        return fileList;
    }

    public void setFileList(Map<String, Boolean> fileList) {
        this.fileList = fileList;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getOwner() {
        return owner;
    }

    public byte getType() {
        return -128;
    }
}

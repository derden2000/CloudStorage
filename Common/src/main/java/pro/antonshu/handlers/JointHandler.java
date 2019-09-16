package pro.antonshu.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.antonshu.service.ComService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

    /* Сервер и Хендлер реализованы в протокольном варианте.
    *  Клиент/сервер могут принимать/отправлять либо файл, либо команды.
    *  Каждая команда имеет 4 обязательных поля:
    *  1. Маркер команды(6 байт);
    *  2. Тип команды(6 байт);
    *  3. Владелец команды(16 байт);
    *  4. Масссив данных(996 байт);
    *
    *  Данный хендлер используется и на сревере, и на клиенте,
    *  поэтому создано 2 конструктора для разделения функций.
    */

public class JointHandler extends ChannelInboundHandlerAdapter {

    private Path path;
    private String owner;
    private byte[] marker = new byte[6];
    private byte[] type = new byte[6];
    private byte[] user = new byte[16];
    private ComService comService;
    private String currentFilename;
    private String userName;
    private boolean serverInstance;
    private static final Logger logger = LogManager.getLogger(JointHandler.class);

    /*
    * Конструктор для клиента и параметром для вывода информации в окно
    */
    public JointHandler(String owner, DefaultListModel<String> fileListModel) throws IOException {
        this.owner = owner;
        this.path = getRootByOS(owner);
        this.comService = new ComService(owner, fileListModel);

    }

    /*
     * Конструктор для сервера(без параметра окна)
     */
    public JointHandler(String owner) throws IOException {
        this.owner = owner;
        this.path = getRootByOS(owner);
        this.comService = new ComService(owner);
        this.serverInstance = true;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuf buf = byteBuf.duplicate();

        buf.readBytes(marker);
        if (new String(marker).equals("marker")) {
            buf.readBytes(type);
            buf.readBytes(user);
            userName = new String(user).trim();
            buf.resetReaderIndex();
            logger.info(owner + " received command: " + new String(type));
            currentFilename = comService.parseMsg(ctx, buf);
        } else {
            receiveFile(byteBuf);
        }
    }



    private Path getRootByOS(String handlerOwner) throws IOException {
        List<File> list = Arrays.asList(File.listRoots());
        String res = list.get(0).toString() + "CS" + File.separator + handlerOwner + File.separator;
        if (!Files.exists(Paths.get(res))) {
            Files.createDirectory(Paths.get(res));
        }
        return Paths.get(res);
    }

    private void receiveFile(ByteBuf byteBuf) throws IOException {
        String pathToWriteFile;
        if (serverInstance) {
            pathToWriteFile = path.toString() + File.separator + userName + File.separator + currentFilename;
        } else {
            pathToWriteFile = path.toString() + File.separator + currentFilename;
        }
        File file = new File(pathToWriteFile);

        if (!file.exists()) {
            file.createNewFile();
        }

        ByteBuffer byteBuffer = byteBuf.nioBuffer();

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        FileChannel fileChannel = randomAccessFile.getChannel();

        while (byteBuffer.hasRemaining()) {
            fileChannel.position(file.length());
            fileChannel.write(byteBuffer);
        }

        byteBuf.release();
        fileChannel.close();
        randomAccessFile.close();
    }

}

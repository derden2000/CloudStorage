package pro.antonshu.service;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ComService {

    private String owner;
    private Path rootPath;
    private byte[] marker = new byte[6];
    private byte[] type = new byte[6];
    private byte[] ownerName = new byte[16];
    private byte[] data = new byte[996];
    private DefaultListModel<String> fileListModel;
    private ChunkService chunkService;



    public ComService(String owner) throws IOException {
        this.owner = owner;
        this.rootPath = getRootByOS(owner);
        this.chunkService = new ChunkService();
    }

    public ComService(String owner, DefaultListModel<String> fileListModel) throws IOException {
        this.owner = owner;
        this.rootPath = getRootByOS(owner);
        this.fileListModel = fileListModel;
        this.chunkService = new ChunkService();
    }

    public String parseMsg(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        buf.readBytes(marker);
        buf.readBytes(type);
        buf.readBytes(ownerName);
        buf.readBytes(data);
        String comType = new String(type);
        String msgOwner = new String(ownerName).trim();
        String dataStr = new String(data).trim();
        String currentFilename = null;

        if (comType.equals("req_fl")) {
            ByteBuf outBuf = Unpooled.copiedBuffer(createFileList(msgOwner));
            ctx.writeAndFlush(outBuf);
        } else if (comType.equals("res_fl")) {
            if (fileListModel != null) {
                fileListModel.clear();
                String[] list = dataStr.split("-");
                for (String str : list) {
                    fileListModel.addElement(str);
                }
            }
        } else if(comType.equals("filena")) {
            currentFilename = dataStr;
        } else if (comType.equals("get_fl")) {
            ByteBuf outBuf2 = Unpooled.copiedBuffer(prepareSendData("filena", owner, data));
            ctx.writeAndFlush(outBuf2);
            chunkService.sendFile(ctx.channel(), rootPath.toString() + File.separator + msgOwner + File.separator + dataStr);
        }
        return currentFilename;
    }




    private byte[] createFileList(String msgOwner) {
        Path pathToFind = Paths.get(rootPath.toString() + File.separator + msgOwner + File.separator);
        if (!pathToFind.toFile().exists()) {
            pathToFind.toFile().mkdir();
        }
        final StringBuilder sb = new StringBuilder();

        try {
            Files.walkFileTree(pathToFind, new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    sb.append(file.getFileName().toString()).append("-");
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prepareSendData("res_fl", msgOwner, sb.toString().getBytes());
    }

    private byte[] prepareSendData(String comType, String user, byte[] data) {
        byte[] total2 = new byte[1024];
        System.arraycopy("marker".getBytes(), 0, total2, 0, "marker".getBytes().length);
        System.arraycopy(comType.getBytes(), 0, total2, 6, comType.getBytes().length);
        System.arraycopy(user.getBytes(), 0, total2, 12, user.getBytes().length);
        if (data != null) {
            System.arraycopy(data, 0, total2, 28, data.length);
        }
        return total2;
    }

    private Path getRootByOS(String handlerOwner) throws IOException {
        List<File> list = Arrays.asList(File.listRoots());
        String res = list.get(0).toString() + "CS" + File.separator + handlerOwner + File.separator;
        if (!Files.exists(Paths.get(res))) {
            Files.createDirectory(Paths.get(res));
        }
        return Paths.get(res);
    }

}

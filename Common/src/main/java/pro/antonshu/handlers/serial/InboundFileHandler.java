package pro.antonshu.handlers.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.antonshu.network.message.FileMessage;
import pro.antonshu.network.message.FileRequest;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InboundFileHandler extends ChannelInboundHandlerAdapter {

    private String rootPath = "d:/ddd/server/";

    private static final Logger logger = LogManager.getLogger(InboundFileHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fr = (FileRequest) msg;
                logger.info(String.format("Получен запрос на файл %s от %s", fr.getFilename(), fr.getOwner()));
                if (Files.exists(Paths.get("d:/ddd/server/" + fr.getFilename()))) {
                    FileMessage fm = new FileMessage(Paths.get("d:/ddd/server/" + fr.getFilename()));
                    ctx.write(fm);
                    logger.info(String.format("Отправлен файл %s пользователю %s", fr.getFilename(), fr.getOwner()));
                }
            } else if (msg instanceof FileMessage) {
                FileMessage fileMessage = (FileMessage) msg;
                if (!fileMessage.getIsParted()) {
                    FileOutputStream fos = new FileOutputStream("D:/ddd/server/" + fileMessage.getFilename());
                    fos.write(fileMessage.getData());
                    logger.info(String.format("Получен файл %s от %s", fileMessage.getFilename(), fileMessage.getOwner()));
                    fos.close();
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

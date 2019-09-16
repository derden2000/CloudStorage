package pro.antonshu.handlers.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.antonshu.network.message.CommandMessage;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class InboundCommandHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LogManager.getLogger(InboundCommandHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }

            if (msg instanceof CommandMessage) {
                CommandMessage cm = (CommandMessage) msg;
                logger.info(String.format("Получена команда: %s от %s", cm.getCommandName(), cm.getOwner()));
                if (cm.getCommandName().equals("request_file_list")) {
                    cm.setFileList(requestList());
                    ctx.write(cm);
                    logger.info(String.format("Отправлен список файлов пользователю %s", cm.getOwner()));
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private Map<String, Boolean> requestList() {
        final Map<String, Boolean> res = new HashMap<>();
        Path root = Paths.get("d:\\ddd\\server\\");

        try
        {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    res.put(file.getFileName().toString(), false);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return res;
    }
}

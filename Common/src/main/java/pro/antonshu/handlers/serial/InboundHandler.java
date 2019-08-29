package pro.antonshu.handlers.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.antonshu.auth.AuthService;
import pro.antonshu.auth.AuthServiceImpl;
import pro.antonshu.network.message.AuthMessage;

public class InboundHandler extends ChannelInboundHandlerAdapter {

    private AuthService authService = new AuthServiceImpl();

    private static final Logger logger = LogManager.getLogger(InboundHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        try {
            if (msg == null) {
                return;
            }
            logger.info("Server received: " + msg.getClass().getSimpleName());
            if (msg instanceof AuthMessage) {
                AuthMessage am = (AuthMessage) msg;
                if (am.getRegMarker()) {
                    if (authService.regNewUser(am.getLogin(), am.getPassword())) {
                        am.setAuthorize(true);
                        ctx.write(am);
                        logger.info(String.format("Зарегистрирован новый пользователь: %s", am.getLogin()));
                    }
                } else if (authService.authUser(am.getLogin(), am.getPassword())) {
                    am.setAuthorize(true);
                    ctx.write(am);
                    logger.info(String.format("Авторизован пользователь: %s", am.getLogin()));
                } else {
                    ctx.write(am);
                    logger.info(String.format("Пользователь не авторизован: %s", am.getLogin()));
                }
            } else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

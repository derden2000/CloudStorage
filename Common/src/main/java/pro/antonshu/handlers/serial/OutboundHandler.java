package pro.antonshu.handlers.serial;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import pro.antonshu.network.message.AuthMessage;
import pro.antonshu.network.message.CommandMessage;
import pro.antonshu.network.message.FileMessage;


public class OutboundHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        try {
            if (msg == null) {
                return;
            }

            if (msg instanceof AuthMessage) {
                AuthMessage am = (AuthMessage) msg;
                ctx.writeAndFlush(am);
                System.out.println("Server send AuthMessage");
            } else if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                ctx.writeAndFlush(fm);
                System.out.println("Server send FileMessage");
            } else if (msg instanceof CommandMessage) {
                CommandMessage cm = (CommandMessage) msg;
                ctx.writeAndFlush(cm);
                System.out.println("Server send CommandMessage");
            }


            else {
                ctx.fireChannelRead(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

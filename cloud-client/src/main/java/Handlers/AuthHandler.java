package Handlers;

import Controller.Callback;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.apache.log4j.Logger;
import javax.swing.*;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    static Logger log = Logger.getLogger("AuthHandler");
    private Callback authOkCallback;

    public AuthHandler(Callback authOkCallback) {
        this.authOkCallback = authOkCallback;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;
        ByteBuf buf = ((ByteBuf) msg);
        if (buf.readableBytes() > (byte) 3) {
            ctx.fireChannelRead(buf);
        } else {
            byte data = buf.readByte();
            if (data == (byte) 2) {
                log.info("STATE: Verification is Not Successful!");
                JOptionPane.showMessageDialog(null, "Вы ввели неверное имя пользователя или пароль!");
            }
            if (data == (byte) 3) {
                // ScreenManager.showWorkFlowScreen();
                log.info("STATE: Verification is Successful!");
                buf.release();
                authOkCallback.callBack();
                ctx.pipeline().remove(this);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

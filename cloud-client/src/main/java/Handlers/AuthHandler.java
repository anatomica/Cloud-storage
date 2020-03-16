package Handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import javax.swing.*;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static int stateOfLogin = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        if (buf.readableBytes() > 3) {
            ctx.fireChannelRead(buf);
        } else {
            byte data = buf.readByte();
            if (data == 2) {
                stateOfLogin = 2;
                System.out.println("STATE: Verification is Not Successful!");
                JOptionPane.showMessageDialog(null, "Вы ввели неверное имя пользователя или пароль!");
            }
            if (data == 3) {
                // ScreenManager.showWorkFlowScreen();
                stateOfLogin = 1;
                System.out.println("STATE: Verification is Successful!");
                buf.release();
            }
        }
    }

    public static String checkLogin() {
        if (stateOfLogin == 0) return "0";
        if (stateOfLogin == 1) return "1";
        if (stateOfLogin == 2) return "2";
        return "0";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

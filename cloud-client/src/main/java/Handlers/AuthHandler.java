package Handlers;

import Controller.ScreenManager;
import Protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    private static int stateOfLogin = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte readed = buf.readByte();

        if (readed == (byte) 3) {
            System.out.println("STATE: Verification is Successful!");
            stateOfLogin = 1;
            buf.release();
        }
        else ctx.fireChannelRead(buf);
    }

    public static boolean checkLogin() {
        if (stateOfLogin == 0) return false;
        return stateOfLogin == 1;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

package Protocol;

import Controller.Controller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProtocolFiles {

    public static void receiveFile(Path path, Channel channel, ChannelFutureListener finishListener) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 15);
        channel.writeAndFlush(buf);
        lines(path, channel, finishListener, buf);
    }

    public static void deleteFile(Path path, Channel channel, ChannelFutureListener finishListener) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 7);
        channel.writeAndFlush(buf);
        lines(path, channel, finishListener, buf);
    }

    private static void lines(Path path, Channel channel, ChannelFutureListener finishListener, ByteBuf buf) {
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt((Controller.pathToFileOfUser + path.getFileName()).length());
        channel.writeAndFlush(buf);

        byte[] filenameBytes = (Controller.pathToFileOfUser + path.getFileName()).getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);

        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}

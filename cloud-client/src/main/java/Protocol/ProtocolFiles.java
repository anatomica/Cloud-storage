package Protocol;

import Controller.Controller;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProtocolFiles {

    public static void refreshFile(String refreshGson, Channel channel) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 4);
        continuation(refreshGson, channel, null, buf);
    }

    public static void authSend(String msgGson, Channel channel) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 5);
        continuation(msgGson, channel, null, buf);
    }

    public static void deleteFile(Path path, Channel channel, ChannelFutureListener finishListener) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 7);
        continuation(Controller.pathToFileOfUser + path.getFileName(), channel, finishListener, buf);
    }

    public static void receiveFile(Path path, Channel channel, ChannelFutureListener finishListener) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 15);
        continuation(Controller.pathToFileOfUser + path.getFileName(), channel, finishListener, buf);
    }

    private static void continuation(String string, Channel channel, ChannelFutureListener finishListener, ByteBuf buf) {
        channel.writeAndFlush(buf);
        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt((string.getBytes(StandardCharsets.UTF_8).length));
        channel.writeAndFlush(buf);
        byte[] filenameBytes = (string.getBytes(StandardCharsets.UTF_8));
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);

        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }

    public static void sendFile(Path path, Channel channel, ChannelFutureListener finishListener) throws IOException {
        FileRegion region = new DefaultFileRegion(new FileInputStream(path.toFile()).getChannel(), 0, path.toFile().length());

        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 25);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt((Controller.pathToFileOfUser + path.getFileName()).getBytes(StandardCharsets.UTF_8).length);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = (Controller.pathToFileOfUser + path.getFileName()).getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(8);
        buf.writeLong(path.toFile().length());
        channel.writeAndFlush(buf);

        ChannelFuture transferOperationFuture = channel.writeAndFlush(region);
        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}

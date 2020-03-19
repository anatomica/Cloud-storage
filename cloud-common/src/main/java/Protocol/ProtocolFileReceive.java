package Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProtocolFileReceive {

    public static void receiveFile(Path path, Channel channel, ChannelFutureListener finishListener) {

        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 15);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(path.getFileName().toString().length());
        channel.writeAndFlush(buf);

        byte[] filenameBytes = path.getFileName().toString().getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        ChannelFuture transferOperationFuture = channel.writeAndFlush(buf);

        if (finishListener != null) {
            transferOperationFuture.addListener(finishListener);
        }
    }
}

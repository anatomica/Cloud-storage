package Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.nio.charset.StandardCharsets;

public class ProtocolRefreshFiles {

    public static void refreshFile(String pathToFiles, Channel channel) {
        refresh(pathToFiles, channel);
    }

    private static void refresh(String refreshGson, Channel channel) {
        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 4);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(refreshGson.getBytes(StandardCharsets.UTF_8).length);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = refreshGson.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }
}

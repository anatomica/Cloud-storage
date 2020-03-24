package Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.nio.charset.StandardCharsets;

public class ProtocolAuthSend {

    public static void authSend(String msgGson, Channel channel) {

        ByteBuf buf;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 5);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(msgGson.getBytes(StandardCharsets.UTF_8).length);
        channel.writeAndFlush(buf);

        byte[] filenameBytes = msgGson.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
    }
}

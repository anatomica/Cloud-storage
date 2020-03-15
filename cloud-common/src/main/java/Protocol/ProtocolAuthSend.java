package Protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ProtocolAuthSend {

    public static void authSend(String msgGson, Channel channel) throws IOException {

        ByteBuf buf = null;
        buf = ByteBufAllocator.DEFAULT.directBuffer(1);
        buf.writeByte((byte) 10);
        channel.writeAndFlush(buf);

        buf = ByteBufAllocator.DEFAULT.directBuffer(4);
        buf.writeInt(msgGson.length());
        channel.writeAndFlush(buf);

        byte[] filenameBytes = msgGson.getBytes(StandardCharsets.UTF_8);
        buf = ByteBufAllocator.DEFAULT.directBuffer(filenameBytes.length);
        buf.writeBytes(filenameBytes);
        channel.writeAndFlush(buf);
        // buf.release();

    }
}

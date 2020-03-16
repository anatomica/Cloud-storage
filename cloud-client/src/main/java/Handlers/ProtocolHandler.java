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

public class ProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE,
        NAME_LENGTH,
        NAME,
        FILE_LENGTH,
        FILE_RECEIVE,
        FILE_SEND,
        AUTHID,
        END
    }

    private State currentState = State.IDLE;
    private int sendFileFromServer = 0;
    private static int stateOfLogin = 0;
    private static int receiveFile = 0;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > 0) {
            if (currentState == State.IDLE) {
                byte readed = buf.readByte();
                if (readed == (byte) 3) {
                    currentState = State.AUTHID;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Verification is Successful!");
                }
                if (readed == (byte) 5) {
                    currentState = State.END;
                    receivedFileLength = 0L;
                    System.out.println("STATE: End of receiving file");
                }
                if (readed == (byte) 15) {
                    sendFileFromServer = 1;
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file sending");
                }
                if (readed == (byte) 25) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else if (readed != (byte) 3 && readed != (byte) 5 && readed != (byte) 15) {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            }

            if (currentState == State.NAME_LENGTH) {
                if (buf.readableBytes() >= 4) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = State.NAME;
                }
            }

            if (currentState == State.NAME) {
                if (buf.readableBytes() >= nextLength) {
                    if (sendFileFromServer == 0) {
                        byte[] fileName = new byte[nextLength];
                        buf.readBytes(fileName);
                        System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                        out = new BufferedOutputStream(new FileOutputStream("client_storage/" + new String(fileName)));
                        currentState = State.FILE_LENGTH;
                    }
                    if (sendFileFromServer == 1) currentState = State.FILE_SEND;
                }
            }

            if (currentState == State.FILE_LENGTH) {
                if (buf.readableBytes() >= 8) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = State.FILE_RECEIVE;
                }
            }

            if (currentState == State.FILE_SEND) {
                sendFileFromServer = 0;
                byte[] fileName = new byte[nextLength];
                buf.readBytes(fileName);
                System.out.println("STATE: Filename what will be send - " + new String(fileName, "UTF-8"));
                if (Files.exists(Paths.get("server_storage/" + new String(fileName)))) {
                    ProtocolFileSender.sendFile(Paths.get("server_storage/" + new String(fileName)), ctx.channel(), future -> {
                        currentState = State.IDLE;
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                        if (future.isSuccess()) {
                            System.out.println("File send successful!");
                            ByteBuf end = ByteBufAllocator.DEFAULT.directBuffer(1);
                            end.writeByte((byte) 5);
                            ctx.channel().writeAndFlush(end);
                        }
                    });
                }
                break;
            }

            if (currentState == State.FILE_RECEIVE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = State.IDLE;
                        System.out.println("File receive successful!");
                        out.close();
                        break;
                    }
                }
            }

            if (currentState == State.AUTHID) {
                // ScreenManager.showWorkFlowScreen();
                stateOfLogin = 1;
                currentState = State.IDLE;
                break;
            }

            if (currentState == State.END) {
                receiveFile = 1;
                currentState = State.IDLE;
                break;
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    public static boolean checkLogin() {
        if (stateOfLogin == 0) return false;
        return stateOfLogin == 1;
    }

    public static boolean checkReceiveFile() {
        if (receiveFile == 0) return false;
        return receiveFile == 1;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

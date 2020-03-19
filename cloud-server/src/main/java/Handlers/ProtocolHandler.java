package Handlers;

import File.*;
import Json.*;
import Protocol.*;
import auth.BaseAuthService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {

    public enum State {
        IDLE,
        NAME_LENGTH,
        NAME,
        FILE_LENGTH,
        FILE_RECEIVE,
        FILE_SEND,
        REFRESH,
        AUTH,
        END
    }

    private State currentState = State.IDLE;
    private int sendFileFromServer = 0;
    private int receiveAuth = 0;
    private int nowRefresh = 0;
    private int nextLength;
    private long fileLength;
    private long receivedFileLength;
    private BufferedOutputStream out;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;
        ByteBuf buf = ((ByteBuf) msg);
        while (buf.readableBytes() > (byte) 0) {
            if (currentState == State.IDLE) {
                byte readByte = buf.readByte();
                if (readByte == (byte) 4) {
                    nowRefresh = 1;
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                }
                if (readByte == (byte) 5) {
                    receiveAuth = 1;
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Authentication is started!");
                }
                if (readByte == (byte) 10) {
                    currentState = State.END;
                    receivedFileLength = 0L;
                    System.out.println("STATE: End of receiving file");
                }
                if (readByte == (byte) 15) {
                    sendFileFromServer = 1;
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file sending");
                }
                if (readByte == (byte) 25) {
                    currentState = State.NAME_LENGTH;
                    receivedFileLength = 0L;
                    System.out.println("STATE: Start file receiving");
                } else if (readByte != (byte) 4 && readByte != (byte) 5 && readByte != (byte) 10 && readByte != (byte) 15) {
                    System.out.println("ERROR: Invalid first byte - " + readByte);
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
                    if (nowRefresh == 1) currentState = State.REFRESH;
                    if (receiveAuth == 1) currentState = State.AUTH;
                    if (sendFileFromServer == 1) currentState = State.FILE_SEND;
                    if (sendFileFromServer == 0 && receiveAuth == 0 && nowRefresh == 0) {
                        byte[] fileName = new byte[nextLength];
                        buf.readBytes(fileName);
                        System.out.println("STATE: Filename received - " + new String(fileName, "UTF-8"));
                        File nameFile = new File("server_storage/" + new String(fileName));
                        nameFile.getParentFile().mkdirs();
                        if (nameFile.createNewFile()) System.out.println("Создан новый файл!");
                        out = new BufferedOutputStream(new FileOutputStream(nameFile, false));
                        currentState = State.FILE_LENGTH;
                    }
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
                            end.writeByte((byte) 10);
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

            if (currentState == State.REFRESH) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] pathToFile = new byte[nextLength];
                    buf.readBytes(pathToFile);
                    System.out.println("STATE: Filename received - " + new String(pathToFile, "UTF-8"));
                    File directory = new File(new String(pathToFile, "UTF-8"));
                    if (!directory.exists()) directory.mkdir();
                    List<FileAbout> fileAbouts = Files.list(Paths.get(new String(pathToFile))).map(Path::toFile).map(FileAbout::new).collect(Collectors.toList());
                    FilesListMessage flm = Message.createFilesList(fileAbouts, "");
                    ProtocolRefreshFiles.sendRefreshFile(flm.toJson(), ctx.channel());
                    nowRefresh = 0;
                    currentState = State.IDLE;
                    break;
                }
            }

            if (currentState == State.AUTH) {
                receiveAuth = 0;
                byte[] fileName = new byte[nextLength];
                buf.readBytes(fileName);
                System.out.println("STATE: Authentication received - " + new String(fileName, "UTF-8"));
                if (BaseAuthService.authentication(new String(fileName, "UTF-8"), ctx.channel())) {
                    System.out.println("Authentication is successful!");
                } else System.out.println("Access denied!");
                currentState = State.IDLE;
                break;
            }

            if (currentState == State.END) {
                currentState = State.IDLE;
                break;
            }
        }
        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

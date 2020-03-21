import handlers.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

class MyServer {

    public void run() throws Exception {
        // Пул потоков для обработки подключений клиентов
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        // Пул потоков для обработки сетевых сообщений
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Создание настроек сервера
            ServerBootstrap b = new ServerBootstrap();
            b.group(mainGroup, workerGroup) // указание пулов потоков для работы сервера
                    .channel(NioServerSocketChannel.class) // указание канала для подключения новых клиентов
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception { // настройка конвеера для каждого подключившегося клиента
                            System.out.println("Подключился новый клиент!");
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new MainHandler()
                            );
                        }
                    });
            ChannelFuture future = b.bind(8190).sync(); // запуск прослушивания порта 8189 для подключения клиентов
            System.out.println("Сервер запущен!");
            future.channel().closeFuture().sync(); // ожидание завершения работы сервера
        } finally {
            mainGroup.shutdownGracefully(); // закрытие пула
            workerGroup.shutdownGracefully(); // закрытие пула
            System.out.println("Сервер завершил работу!");
        }
    }
}

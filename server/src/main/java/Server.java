import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Server {

    private static EventLoopGroup auth;
    private static EventLoopGroup worker;
    private static final int MAX_FILE_SIZE = 200 * 1024 * 1024;
    private static DBStorage dbStorage;

    public Server() throws Exception {

        auth = new NioEventLoopGroup();
        worker = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(MAX_FILE_SIZE,
                                            ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new AuthHandler(dbStorage)
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(8189).sync();

            log.debug("Server started");

            dbStorage = new DBStorage();
            future.channel().closeFuture().sync();

        } finally {

            auth.shutdownGracefully();
            worker.shutdownGracefully();
            DBStorage.closedb();

        }
    }

    public static void main(String[] args) throws Exception {
        new Server();
    }
}

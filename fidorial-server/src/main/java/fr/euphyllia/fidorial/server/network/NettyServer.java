package fr.euphyllia.fidorial.server.network;

import fr.euphyllia.fidorial.server.FidorialServer;
import fr.euphyllia.fidorial.server.network.codec.FrameDecoder;
import fr.euphyllia.fidorial.server.network.codec.FrameEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;

public final class NettyServer {

    private static final ComponentLogger LOGGER = ComponentLogger.logger(NettyServer.class);
    private final FidorialServer server;
    private final int port;
    private final MultiThreadIoEventLoopGroup bossGroup;
    private final MultiThreadIoEventLoopGroup workerGroup;
    private final Class<? extends ServerChannel> channelClass;
    private @Nullable Channel channel;

    public NettyServer(final FidorialServer server, final int port) {
        this.server = server;
        this.port = port;

        if (server.config().network().useIoUring() && IoUring.isAvailable()) {
            LOGGER.debug("Using io_uring transport");
            bossGroup = new MultiThreadIoEventLoopGroup(1, IoUringIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(IoUringIoHandler.newFactory());
            channelClass = IoUringServerSocketChannel.class;
        } else if (Epoll.isAvailable()) {
            LOGGER.debug("Using epoll transport");
            bossGroup = new MultiThreadIoEventLoopGroup(1, EpollIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(EpollIoHandler.newFactory());
            channelClass = EpollServerSocketChannel.class;
        } else if (KQueue.isAvailable()) {
            LOGGER.debug("Using kqueue transport");
            bossGroup = new MultiThreadIoEventLoopGroup(1, KQueueIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(KQueueIoHandler.newFactory());
            channelClass = KQueueServerSocketChannel.class;
        } else {
            LOGGER.debug("Using NIO transport");
            bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
            channelClass = NioServerSocketChannel.class;
        }
    }

    public void bind() throws InterruptedException {
        final ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(channelClass)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(final SocketChannel ch) {
                        ch.pipeline()
                                .addLast("frame-decoder", new FrameDecoder())
                                .addLast("frame-encoder", new FrameEncoder())
                                .addLast("handler", new ClientConnection(server));
                    }
                });
        this.channel = bootstrap.bind(port).sync().channel();
    }

    public void shutdown() {
        if (channel != null) channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}

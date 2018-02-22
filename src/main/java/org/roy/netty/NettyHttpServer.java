package org.roy.netty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * <p>
 * Created: Feb 22, 2018 9:51:52 AM
 * </p>
 * 
 * @author luoyh(Roy)
 * @version 1.0
 * @since 1.7
 */
public class NettyHttpServer {

    public static void main(String[] args) {
        try {
            BindingMeta meta = new BindingMeta("org.roy.netty");

            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            ServerBootstrap s = new ServerBootstrap();
            s.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    // server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
                    ch.pipeline().addLast(new HttpRequestDecoder());
                    // server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast(new SimpleHttpRequestHandler(meta));
                    // ch.pipeline().addLast(new HttpServerHandler());
                }
            }).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = s.bind(18088).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static class SimpleHttpRequestHandler extends ChannelInboundHandlerAdapter {

        private final BindingMeta meta;
        private final ObjectMapper mapper = new ObjectMapper();

        public SimpleHttpRequestHandler(BindingMeta meta) {
            this.meta = meta;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object e) throws Exception {

            if (e instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) e;
                String uri = request.uri();
                System.out.println("Rest Request:" + uri);

                if (!"/favicon.ico".equals(uri)) { // 过滤静态资源，因为不需要前端页面
                    Object ret = meta.invoke(request); // 触发controller逻辑
                    // writeResponse(e.getChannel(), ret != null ? ret : "done"); // 返回
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                            Unpooled.wrappedBuffer(mapper.writeValueAsBytes(ret)));
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.write(response);
                } else {
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                    ctx.write(response);
                }
                ctx.flush();
            }

        }

        // 这个方法将返回值以Json方式返回，类似实现Spring的@ResponseBody注解
        // private void writeResponse(Channel channel, Object obj) {
        //
        // HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        // response.setStatus(HttpResponseStatus.OK);
        //
        // response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        //
        // String resMsg = JsonUtils.toJsonString(obj); // 序列化方法，不用展开了吧
        // response.setContent(ChannelBuffers.wrappedBuffer(resMsg.getBytes()));
        // final ChannelFuture future = channel.write(response);
        // future.addListener(ChannelFutureListener.CLOSE);
        // }
    }

}

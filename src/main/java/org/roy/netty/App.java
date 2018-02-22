package org.roy.netty;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * Hello world!
 *
 */
public class App {
    private static int idx = 0;
    public static void main(String[] args) throws Exception {
        Bootstrap b = new Bootstrap();
        EventLoopGroup g = new NioEventLoopGroup();
        b.group(g)
        .channel(NioSocketChannel.class)
        .option(ChannelOption.SO_KEEPALIVE, true)
        // .handler(new Handler());
        .handler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
                ch.pipeline().addLast(new HttpResponseDecoder());
                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(new HttpClientInboundHandler());
            }
        });
        //b.bind("192.168.11.221", 1111).sync().channel().closeFuture().await();
        
        // Start the client.
        ChannelFuture f = b.connect("192.168.11.221", 1111).sync();
        
        while (true) {
            new Thread(() -> {
                //URI uri = new URI("/GDDC/human/educationInfo");
                 try {
                     URI uri = new URI(idx ++ % 2 == 0 ? "/GDDC/safeProdectDays/daysCount" : "/GDDC/human/educationInfo");
                     String msg = "Are you ok?";
                     DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, 
                             HttpMethod.GET,
                             uri.toASCIIString(), 
                             Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));

                     //request.retain();
                     
                     
                     
                     // 构建http请求
                     request.headers().set(HttpHeaderNames.HOST, "192.168.11.221");
                     request.headers().set(HttpHeaderNames.HOST, "");
                     request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                     request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
                     
                     // 发送http请求
                     f.channel().write(request);
                     f.channel().flush();
                     f.channel().closeFuture().sync();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }).start();
            TimeUnit.SECONDS.sleep(2);
        }
        
        
        
    }

    private static class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;
                ByteBuf buf = content.content();
                System.out.println(buf.toString(CharsetUtil.UTF_8));
                buf.release();
            }
        }
    }
    
    private static class Handler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            System.out.println("1:" + msg);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("2:" + ctx);
            super.channelActive(ctx);
        }

    }
}

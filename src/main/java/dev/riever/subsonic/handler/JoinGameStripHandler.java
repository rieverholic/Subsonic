package dev.riever.subsonic.handler;

import dev.riever.subsonic.protocol.VelocityInternals;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Set;

public final class JoinGameStripHandler extends ChannelInboundHandlerAdapter {

    private final Set<String> serverNames;

    public JoinGameStripHandler(Set<String> serverNames) {
        this.serverNames = serverNames;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String serverName = VelocityInternals.getServerName(ctx.channel());
        if (serverNames.contains(serverName) && msg instanceof ByteBuf buf) {
            try {
                tryStripAndPass(ctx, buf);
            } finally {
                buf.release();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void tryStripAndPass(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (buf.readableBytes() < 4 || !VelocityInternals.isJoinGamePacket(ctx.channel(), buf)) {
            ctx.fireChannelRead(buf.retain());
            return;
        }
        Channel serverChannel = VelocityInternals.getServerChannel(ctx.channel());
        if (serverChannel == null) {
            ctx.fireChannelRead(buf.retain());
            return;
        }
        int dimIdStart = buf.writerIndex() - 4;
        int dimId = buf.getInt(dimIdStart);
        serverChannel.attr(JoinGameAppendHandler.GTNH_DIM_ID).set(dimId);
        buf.writerIndex(dimIdStart);
        ctx.fireChannelRead(buf.retain());
    }
}

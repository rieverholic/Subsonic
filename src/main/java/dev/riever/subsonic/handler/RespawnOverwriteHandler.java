package dev.riever.subsonic.handler;

import dev.riever.subsonic.protocol.VelocityInternals;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

public class RespawnOverwriteHandler extends ChannelOutboundHandlerAdapter {

    public static final AttributeKey<Integer> GTNH_DIM_ID_RESPAWN = AttributeKey.valueOf("gtnh-dim-id-respawn");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf buf) {
            try {
                tryOverwriteAndPass(ctx, buf, promise);
            } finally {
                buf.release();
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    private void tryOverwriteAndPass(ChannelHandlerContext ctx, ByteBuf buf, ChannelPromise promise) {
        if (VelocityInternals.isRespawnPacket(ctx.channel(), buf) && buf.readableBytes() >= 1 + Integer.BYTES) {
            Integer dimId = ctx.channel().attr(GTNH_DIM_ID_RESPAWN).getAndSet(null);
            if (dimId != null) {
                buf.setInt(buf.readerIndex() + 1, dimId);
            }
        }
        ctx.write(buf.retain(), promise);
    }
}

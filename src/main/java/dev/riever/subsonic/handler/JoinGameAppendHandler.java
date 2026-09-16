package dev.riever.subsonic.handler;

import dev.riever.subsonic.protocol.VelocityInternals;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

public class JoinGameAppendHandler extends ChannelOutboundHandlerAdapter {

    public static final AttributeKey<Integer> GTNH_DIM_ID = AttributeKey.valueOf("gtnh-dim-id");

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf buf) {
            try {
                tryAppendAndPass(ctx, buf, promise);
            } finally {
                buf.release();
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    private void tryAppendAndPass(ChannelHandlerContext ctx, ByteBuf buf, ChannelPromise promise) {
        if (VelocityInternals.isJoinGamePacket(ctx.channel(), buf)) {
            Integer dimId = ctx.channel().attr(GTNH_DIM_ID).getAndSet(null);
            if (dimId == null) {
                ctx.write(buf.retain(), promise);
                return;
            }
            ByteBuf out = buf.alloc().buffer(buf.readableBytes() + 4);
            out.writeBytes(buf);
            out.writeInt(dimId);
            ctx.write(out, promise);
            return;
        }
        ctx.write(buf.retain(), promise);
    }
}

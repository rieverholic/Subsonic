package dev.riever.subsonic.handler;

import dev.riever.subsonic.protocol.MinecraftPackets;
import dev.riever.subsonic.protocol.VelocityInternals;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

public final class DimensionPatchHandler extends ChannelOutboundHandlerAdapter {

    public static final AttributeKey<Integer> GTNH_DIM_ID = AttributeKey.valueOf("gtnh-dim-id");

    private static final int JOIN_GAME_DIM_OFFSET = Integer.BYTES + 1;
    private Integer realDim;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf buf) {
            try {
                tryPatchAndPass(ctx, buf, promise);
            } finally {
                buf.release();
            }
        } else {
            ctx.write(msg, promise);
        }
    }

    public void tryPatchAndPass(ChannelHandlerContext ctx, ByteBuf buf, ChannelPromise promise) throws Exception {
        MinecraftPackets.VarInt id = MinecraftPackets.readVarInt(buf);
        if (id == null) {
            ctx.write(buf.retain(), promise);
            return;
        }
        MinecraftPackets.Protocol proto = VelocityInternals.getProtocol(ctx.channel(), true);
        int body = buf.readerIndex() + id.bytesRead();

        if (MinecraftPackets.isLegacyJoinGamePacket(proto, id.value())) {
            realDim = ctx.channel().attr(GTNH_DIM_ID).getAndSet(null);
            if (realDim != null && buf.readableBytes() > id.bytesRead() + JOIN_GAME_DIM_OFFSET
                    && buf.getByte(body + JOIN_GAME_DIM_OFFSET) == realDim.byteValue()) {
                ByteBuf out = buf.alloc().buffer(buf.readableBytes() + Integer.BYTES);
                out.writeBytes(buf).writeInt(realDim);
                ctx.write(out, promise);
                return;
            }
        } else if (realDim != null && MinecraftPackets.isLegacyRespawnPacket(proto, id.value())
                && buf.readableBytes() >= id.bytesRead() + Integer.BYTES) {
            if (buf.getInt(body) == realDim.byteValue()) {
                buf.setInt(body, realDim);
            }
        }
        ctx.write(buf.retain(), promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) {
        realDim = null;
        ctx.flush();
    }
}

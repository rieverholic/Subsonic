package dev.riever.subsonic.protocol;

import io.netty.buffer.ByteBuf;

public class MinecraftPackets {
    public record VarInt(int value, int bytesRead) {}

    public record Protocol(String version, String state, boolean clientBound) {}

    public static VarInt readVarInt(ByteBuf buf) {
        int offset = buf.readerIndex();
        int end = buf.writerIndex();
        int value = 0;
        int position = 0;
        while (offset < end) {
            byte b = buf.getByte(offset++);
            value |= (b & 0x7F) << position;
            if ((b & 0x80) == 0) {
                return new VarInt(value, offset - buf.readerIndex());
            }
            position += 7;
            if (position >= 32) return null; // malformed
        }
        return null; // incomplete
    }

    public static boolean isLegacyJoinGamePacket(Protocol proto, int packetId) {
        return proto != null && proto.version().equals("MINECRAFT_1_7_6") && proto.state().equals("PLAY") && proto.clientBound() && packetId == 0x01;
    }
}

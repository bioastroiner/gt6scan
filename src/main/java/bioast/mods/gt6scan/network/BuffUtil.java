package bioast.mods.gt6scan.network;

import io.netty.buffer.ByteBuf;

public class BuffUtil {
    public static void writeOreData(ByteBuf buffer, OreData data) {
        buffer.writeInt(data.x);
        buffer.writeInt(data.y);
        buffer.writeInt(data.z);
        buffer.writeShort(data.matID);
    }

    public static OreData readOreData(ByteBuf buffer) {
        return new OreData(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readShort());
    }
}

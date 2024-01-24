package bioast.mods.gt6scan.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class ScanRequestToServer implements IMessage {
    int mode;
    int x, z;

    public ScanRequestToServer(ScanMode mode, int x, int z) {
        this.mode = mode.ordinal();
        this.x = x;
        this.z = z;
    }

    public ScanRequestToServer() {
        // invalid
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readInt();
        x = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(mode);
        buf.writeInt(x);
        buf.writeInt(z);
    }
}

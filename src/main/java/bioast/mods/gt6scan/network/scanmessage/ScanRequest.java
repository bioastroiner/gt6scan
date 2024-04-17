package bioast.mods.gt6scan.network.scanmessage;

import bioast.mods.gt6scan.network.ScanMode;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class ScanRequest implements IMessage {
    int mode;
    int x, z;
    int chunkSize;

    public ScanRequest(ScanMode mode, int x, int z, int chunkSize) {
        this(mode, x, z);
        this.chunkSize = chunkSize;
    }

    public ScanRequest(ScanMode mode, int x, int z) {
        this.mode = mode.ordinal();
        this.x = x;
        this.z = z;
        chunkSize = 9;
    }

    public ScanRequest() {
        // invalid
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        mode = buf.readInt();
        x = buf.readInt();
        z = buf.readInt();
        chunkSize = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(mode);
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(chunkSize);
    }
}

package bioast.mods.gt6scan.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ScanResponceToClient implements IMessage {
    List<OreData> scannedOres;
    int x, z, mode;

    public ScanResponceToClient(List<OreData> scannedOres, int x, int z, int mode) {
        this.scannedOres = scannedOres;
        this.x = x;
        this.z = z;
        this.mode = mode;
    }

    public ScanResponceToClient() {
        scannedOres = new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        scannedOres = new ArrayList<>();
        x = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            scannedOres.add(BuffUtil.readOreData(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int size;
        if (buf instanceof AbstractByteBuf buffer) {
            size = buffer.maxCapacity();
        }
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(scannedOres.size());
        for (OreData ore : scannedOres) {
            BuffUtil.writeOreData(buf, ore);
        }
    }
}

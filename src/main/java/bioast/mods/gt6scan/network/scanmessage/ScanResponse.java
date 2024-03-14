package bioast.mods.gt6scan.network.scanmessage;

import bioast.mods.gt6scan.network.OreData;
import bioast.mods.gt6scan.utils.BuffUtil;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ScanResponse implements IMessage {
    List<OreData> scannedOres;
    int x, z, mode;

    public ScanResponse(List<OreData> scannedOres, int x, int z, int mode) {
        this.scannedOres = scannedOres;
        this.x = x;
        this.z = z;
        this.mode = mode;
    }

    public ScanResponse() {
        scannedOres = new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        scannedOres = new ArrayList<>();
        x = buf.readInt();
        z = buf.readInt();
        int size = buf.readInt();
        mode = buf.readInt();
        for (int i = 0; i < size; i++) {
            scannedOres.add(BuffUtil.readOreData(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(z);
        buf.writeInt(scannedOres.size());
        buf.writeInt(mode);
        for (OreData ore : scannedOres) {
            BuffUtil.writeOreData(buf, ore);
        }
    }
}

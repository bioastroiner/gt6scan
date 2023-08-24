package bioast.mods.gt6m.network;

import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

public class OreSyncHandler extends ValueSyncHandler<MapData> {


    @Override
    public MapData getCachedValue() {
        return null;
    }

    @Override
    public void setValue(MapData value) {

    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return false;
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {

    }

    @Override
    public void read(PacketBuffer buffer) {

    }

    @Override
    public void updateFromClient(MapData value) {

    }
}

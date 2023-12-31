package bioast.mods.gt6scan.network;

import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OreDataSyncHandler extends ValueSyncHandler<List<OreData>> {

    private final Supplier<List<OreData>> getter;
    private final Consumer<List<OreData>> setter;
    private List<OreData> cache;
    private int size;

    public OreDataSyncHandler(Supplier<List<OreData>> getter, Consumer<List<OreData>> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
        this.size = cache.size();
    }

    @Override
    public List<OreData> getCachedValue() {
        return this.cache;
    }

    @Override
    public void setValue(List<OreData> value) {
        this.cache = value;
    }

    @Override
    public boolean needsSync(boolean isFirstSync) {
        return isFirstSync || this.cache != this.getter.get();
    }

    @Override
    public void updateAndWrite(PacketBuffer buffer) {
        setValue(this.getter.get());
        buffer.writeInt(cache.size());
        for (OreData data : cache) {
            buffer.writeInt(data.x);
            buffer.writeInt(data.y);
            buffer.writeInt(data.z);
            buffer.writeShort(data.matID);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.cache = new ArrayList<>();
        this.size = buffer.readInt();
        for (int i = 0; i < this.size; i++) {
            OreData data = new OreData(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readShort());
            this.cache.add(data);
        }
        setValue(this.cache);
        this.setter.accept(getCachedValue());
    }

    @Override
    public void updateFromClient(List<OreData> value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }
}

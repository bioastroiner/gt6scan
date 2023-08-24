package bioast.mods.gt6m.network;

import com.cleanroommc.modularui.api.sync.ValueSyncHandler;
import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OreSyncHandler extends ValueSyncHandler<List<DataOre>> {

    private final Supplier<List<DataOre>> getter;
    private final Consumer<List<DataOre>> setter;
    private List<DataOre> cache;
    private int size;

    public OreSyncHandler(Supplier<List<DataOre>> getter, Consumer<List<DataOre>> setter) {
        this.getter = getter;
        this.setter = setter;
        this.cache = getter.get();
        this.size = cache.size();
    }

    @Override
    public List<DataOre> getCachedValue() {
        return this.cache;
    }

    @Override
    public void setValue(List<DataOre> value) {
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
        for (DataOre data : cache) {
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
            DataOre data = new DataOre(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readShort());
            this.cache.add(data);
        }
        setValue(this.cache);
        this.setter.accept(getCachedValue());
    }

    @Override
    public void updateFromClient(List<DataOre> value) {
        this.setter.accept(value);
        syncToServer(0, this::updateAndWrite);
    }
}

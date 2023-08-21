package bioast.mods.gt6m.scanner;

import gregapi.oredict.OreDictMaterial;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import static bioast.mods.gt6m.GT6M_Mod.proxy;

public class PacketScanner {
    public final int chunkX;
    public final int chunkZ;
    public final int posX;
    public final int posZ;
    public final int size;
    /**
     * [x,z] (y -> mat)
     */
    public final HashMap<Byte, Short>[][] MAP_MAT;
    public final HashMap<String, Integer> ORES_TO_RGB; // use decimals
    public final HashMap<Short, String> META_TO_NAME;
    public PacketScanner(int chunkX, int chunkZ, int posX, int posZ, int size){
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.posX = posX;
        this.posZ = posZ;
        this.size = size;
        this.MAP_MAT = new HashMap[getSize()][getSize()];
        this.ORES_TO_RGB = new HashMap<>();
        this.META_TO_NAME = new HashMap<>();
    }
    public int getPacketID() {
        return 0;
    }

    public void encode(OutputStream out) throws IOException {
        // Write Data
        DataOutputStream stream = new DataOutputStream(new GZIPOutputStream(out));stream.writeInt(chunkX);stream.writeInt(chunkZ);stream.writeInt(posX);stream.writeInt(posZ);stream.writeInt(size);
        int aSize = (size * 2 + 1) * 16;
        int checked = 0;
        for (int i = 0; i < aSize; i++) {
            for (int j = 0; j < aSize; j++) {
                HashMap<Byte, Short> data = MAP_MAT[i][j];
                if(data!=null){
                     stream.writeByte(data.keySet().size());
                    for (byte y: data.keySet()) {
                        stream.writeByte(y);
                        stream.writeShort(data.get(y));
                        //mapOre(MAP_MAT[i][j].put());
                        checked++;
                    }
                }
                else stream.writeByte(0);
            }
            stream.writeShort(checked);
            stream.close();
        }
    }

    public static Object decode(InputStream in) throws IOException {
        // Read Data, Recieved on Client?
        // Add Ores to List Here
        DataInput aData = new DataInputStream(new java.util.zip.GZIPInputStream(in));
        PacketScanner packet = new PacketScanner(aData.readInt(),aData.readInt(),aData.readInt(),aData.readInt(),aData.readInt());
        int aSize = (packet.size * 2 + 1) * 16;
        int checked = 0;
        for (int i = 0; i < aSize; i++) {
            for (int j = 0; j < aSize; j++) {
                byte kSize = aData.readByte();
                // for getting highest y level
                if(kSize==0) continue;
                packet.MAP_MAT[i][j] = new HashMap<>();
                for (int k = 0; k < kSize; k++) {
                    final byte y = aData.readByte();
                    final short meta = aData.readShort();
                    packet.MAP_MAT[i][j].put(y, meta);
                    mapOre(meta, packet);
                    checked++;
                }
            }
        }
        // validate packet
        int checked_expected = aData.readInt();
        if(checked!=checked_expected) return null;
        return packet;
    }

    public static void mapOre(short meta, PacketScanner packet) {
        // TODO Add Ores Here to the map
        OreDictMaterial tMaterial = OreDictMaterial.MATERIAL_ARRAY[meta];
        short[] rgba = tMaterial.fRGBaSolid;
        int rgba_hex = ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF));
        packet.ORES_TO_RGB.put(tMaterial.mNameInternal,rgba_hex);
        packet.META_TO_NAME.put(meta, tMaterial.mNameInternal);
    }

    public void process() {
        proxy.openProspectorGUI();
    }

    public int getSize(){
        return (size * 2 + 1) * 16;
    }

    public void addBlock(int x, int y, int z, short metaData) {
        int aX = x - (chunkX - size) * 16;
        int aZ = z - (chunkZ - size) * 16;
        if (MAP_MAT[aX][aZ] == null) MAP_MAT[aX][aZ] = new HashMap<>();
        MAP_MAT[aX][aZ].put((byte) y, metaData);
    }
}

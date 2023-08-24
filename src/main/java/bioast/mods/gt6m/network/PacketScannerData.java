package bioast.mods.gt6m.network;

import bioast.mods.gt6m.GT6M_Mod;
import com.cleanroommc.modularui.manager.GuiInfos;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gregapi.network.INetworkHandler;
import gregapi.network.IPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PacketScannerData implements IPacket {

    /* new genius way */
    List<DataOre> oreDataList = new ArrayList<>();
    private int mDecoderType = 0;

    public PacketScannerData() {

    }
    /* old stupid data */
//    public short[][] materialAt;
//    public byte[][] yArrays;
//    public boolean[][] existsAt;
//    public Pair<Integer,Integer>[][] posAt;

    public PacketScannerData(int mDecoderType) {
        this.mDecoderType = mDecoderType;
    }

    public void addOreHere(int x, int y, int z, short matID) {
        oreDataList.add(new DataOre(x, y, z, matID));
    }

    @Override
    public byte getPacketID() {
        return 0;
    }

//    public PacketScannerData setSize(int size){
//        this.size = (byte) size;
//        this.existsAt = new boolean[size*16][size*16];
//        this.yArrays = new byte[size*16][size*16];
//        this.materialAt = new short[size*16][size*16];
//        return this;
//    }
//
//    public PacketScannerData put(int x, int y, int z, short matID){
//        materialAt[x][z] = matID;
//        yArrays[x][z]= (byte) y;
//        existsAt[x][z]=true;
//        return this;
//    }

    @Override
    public ByteArrayDataOutput encode() {
        ByteArrayDataOutput o = ByteStreams.newDataOutput();
        o.write(oreDataList.size());
        for (DataOre ore : oreDataList) {
            o.write(ore.x);
            o.write(ore.y);
            o.write(ore.z);
            o.writeShort(ore.matID);
        }
//        materialAt = new short[size][size];
//        outputData.writeByte(size);
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                short matID = materialAt[i][j];
//                outputData.writeBoolean(existsAt[i][j]);
//                if(existsAt[i][j]){
//                    outputData.writeInt(i);
//                    outputData.writeByte(yArrays[i][j]);
//                    outputData.writeInt(j);
//                    outputData.writeShort(matID);
//                } else {
//                    materialAt[i][j]=0;
//                }
//            }
//        }
        return o;
    }

    @Override
    public IPacket decode(ByteArrayDataInput in) {
        int count = in.readInt();
        List<DataOre> dataOre = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            dataOre.add(new DataOre(in.readInt(), in.readInt(), in.readInt(), in.readShort()));
        }

//        size=inputData.readByte();
//        for (int i = 0; i < size; i++) {
//            for (int j = 0; j < size; j++) {
//                boolean exists = inputData.readBoolean();
//                if(exists){
//                    int x = inputData.readInt();
//                    byte y = inputData.readByte();
//                    int z = inputData.readInt();
//                    short matID = inputData.readShort();
//                    materialAt[i][j] = matID;
//                    yArrays[i][j]=y;
//                } else{
//                    materialAt[i][j] = 0;
//                    yArrays[i][j] = 0;
//                }
//            }
//        }
        return this;
    }

    @Override
    public void process(IBlockAccess aWorld, INetworkHandler aNetworkHandler) {
        if (aWorld instanceof World) {
            GuiInfos.PLAYER_ITEM_MAIN_HAND.open(Minecraft.getMinecraft().thePlayer);
            GT6M_Mod.debug.info("process on client");

            //Item item = Minecraft.getMinecraft().thePlayer.getHeldItem().getItem();
//            if(item instanceof bioast.mods.gt6m.scanner.ScannerMultiTool)
//            // we are in client right?
//            {
//                ScannerMultiTool scannerTool = (ScannerMultiTool) item;
//
//                GT6M_Mod.LOG.info("process on client");
//            }
            //aNetworkHandler.getChannel(Side.CLIENT).generatePacketFrom(this);
        }
    }

}

package bioast.mods.gt6mapper.world;

import gregapi.oredict.OreDictMaterial;
import gregapi.util.UT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.MapData;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/*
    Stores Geographical Map Information on world
    it also tracks a brief snapshot of whenever
    it was taken of Ores it countains
    which is immutable (not runtime)
    new data is overwriten not updated!
 */
public class ProspectMapData extends MapData {
    public Map<Short,Integer> mapDictionary_Normal = new HashMap<>();
    public Map<Short,Integer> mapDictionary_Small = new HashMap<>();
    public Map<Short,Integer> mapDictionary_Dense = new HashMap<>();

	public ProspectMapData(String map_ID) {
		super(map_ID);
	}

    public void put(short id,int state){
        if(state>2) throw new IllegalArgumentException("state cant be more than 2");
        if(OreDictMaterial.MATERIAL_ARRAY[id]==null) throw new IllegalArgumentException("invalid ID");
        int count = 0;
        switch (state){
            case 0:
                count = mapDictionary_Normal.getOrDefault(id,0);
                mapDictionary_Normal.put(id,count + 1);
                break;
            case 1:
                count = mapDictionary_Small.getOrDefault(id,0);
                mapDictionary_Small.put(id,count + 1);
                break;
            case 2:
                count = mapDictionary_Dense.getOrDefault(id,0);
                mapDictionary_Dense.put(id,count + 1);
                break;
        }
    }

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readFromNBT(par1NBTTagCompound);
        NBTTagCompound dummyTag = UT.NBT.make("id",(short)1,"count",(int)10000); // a tag with the same byte Type as other tags in the  ore list
        mapDictionary_Normal = new HashMap<>();
        mapDictionary_Small = new HashMap<>();
        mapDictionary_Dense = new HashMap<>();
        NBTTagList list;
        list=par1NBTTagCompound.getTagList("oresNormal",dummyTag.getId());
        for (int i = 0; i < list.tagCount(); i++) mapDictionary_Normal.put(list.getCompoundTagAt(i).getShort("id"),list.getCompoundTagAt(i).getInteger("count"));
        list=par1NBTTagCompound.getTagList("oresSmall",dummyTag.getId());
        for (int i = 0; i < list.tagCount(); i++) mapDictionary_Small.put(list.getCompoundTagAt(i).getShort("id"),list.getCompoundTagAt(i).getInteger("count"));
        list=par1NBTTagCompound.getTagList("oresDense",dummyTag.getId());
        for (int i = 0; i < list.tagCount(); i++) mapDictionary_Dense.put(list.getCompoundTagAt(i).getShort("id"),list.getCompoundTagAt(i).getInteger("count"));
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeToNBT(par1NBTTagCompound);
        // store Ore IDs and map them to their amounts
        /*
        oresNormal[
          {
          id: 12345
          count: 122345
          }....
        ],....
         */
        NBTTagList[] lists = new NBTTagList[3];
        NBTTagList list = new NBTTagList();
        mapDictionary_Normal.forEach((id,count)->list.appendTag(UT.NBT.make("id",id,"count",count)));
        NBTTagList list1 = new NBTTagList();
        mapDictionary_Small.forEach((id,count)->list1.appendTag(UT.NBT.make("id",id,"count",count)));
        NBTTagList list2 = new NBTTagList();
        mapDictionary_Dense.forEach((id,count)->list2.appendTag(UT.NBT.make("id",id,"count",count)));
        par1NBTTagCompound.setTag("oresNormal", list);
        par1NBTTagCompound.setTag("oresSmall", list1);
        par1NBTTagCompound.setTag("oresDense",list2);
    }
}

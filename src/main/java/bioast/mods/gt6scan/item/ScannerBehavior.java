package bioast.mods.gt6scan.item;

import bioast.mods.gt6scan.network.ScanMode;
import bioast.mods.gt6scan.network.scanmessage.ScanRequest;
import bioast.mods.gt6scan.proxy.CommonProxy;
import gregapi.code.TagData;
import gregapi.data.CS;
import gregapi.data.LH.Chat;
import gregapi.item.multiitem.MultiItem;
import gregapi.item.multiitem.behaviors.IBehavior;
import gregapi.util.UT;
import gregapi.util.UT.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ScannerBehavior extends IBehavior.AbstractBehaviorDefault {
    private final TagData EnergyType; // if null wont consume energy

    public ScannerBehavior() {
        this(null);
    }

    public ScannerBehavior(TagData energyType) {
        EnergyType = energyType;
    }

    @Override
    public ItemStack onItemRightClick(MultiItem aItem, ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
        int tier = aStack.getItemDamage();
        ScanMode mode = getMode(aStack);
        setSize(aStack, tier * 2 - 1);
        if (!aPlayer.isSneaking()) {
            if (mode != ScanMode.NONE) {
                if (tryConsumeEnergy(aItem, aStack, aWorld, aPlayer, tier) && aWorld.isRemote) {
                    ScanRequest req = new ScanRequest(mode, (int) aPlayer.posX, (int) aPlayer.posZ, getSize(aStack));
                    CommonProxy.simpleNetworkWrapper.sendToServer(req);
                }
            }
        } else {
            return nextMode(aPlayer, aStack, mode);
        }
        return super.onItemRightClick(aItem, aStack, aWorld, aPlayer);
    }

    @Override
    public List<String> getAdditionalToolTips(MultiItem aItem, List<String> aList, ItemStack aStack) {
        aList.add(Chat.GRAY + "Shift Right Click to Change Mode.");
        aList.add(Chat.GRAY + "Right Click to Open GUI.");
        aList.add(Chat.BLINKING_ORANGE + ScanMode.values()[NBT.getOrCreate(aStack)
            .getInteger("mode")] + Chat.GRAY + " Mode.");
        aList.add(Chat.GRAY + String.format("Size: %s%d", Chat.BLINKING_ORANGE, getSize(aStack)));
        return aList;
    }

    private ScanMode getMode(ItemStack aStack) {
        NBTTagCompound tag = UT.NBT.getOrCreate(aStack);
        if (!tag.hasKey("mode")) {
            UT.NBT.makeInt(tag, 0);
            UT.NBT.set(aStack, tag);
            return ScanMode.LARGE; // default to large
        } else {
            return ScanMode.values()[tag.getInteger("mode")];
        }
    }

    private ItemStack setSize(ItemStack aStack, int size) {
        UT.NBT.makeInt(UT.NBT.getNBT(aStack), "size", size);
        return aStack;
    }

    @Nullable
    private boolean tryConsumeEnergy(MultiItem aItem,
                                     ItemStack aStack,
                                     World aWorld,
                                     EntityPlayer aPlayer,
                                     int tier) {
        if (!UT.Entities.isCreative(aPlayer)) {
            if (getEnergy(aItem, aStack) < CS.V[tier] && EnergyType != null) return true;
            else if (!aWorld.isRemote)
                return aItem.useEnergy(EnergyType,
                    aStack,
                    ScannerMultiTool.consumptionRate * CS.V[tier],
                    aPlayer,
                    aPlayer.inventory,
                    aWorld,
                    (int) aPlayer.posX,
                    (int) aPlayer.posY,
                    (int) aPlayer.posZ,
                    !UT.Entities.isCreative(aPlayer));
        }
        return true;
    }

    private int getSize(ItemStack aStack) {
        NBTTagCompound tag = UT.NBT.getOrCreate(aStack);
        if (!tag.hasKey("size")) {
            UT.NBT.makeInt(tag, 1);
            UT.NBT.set(aStack, tag);
            return 1;
        } else {
            return tag.getInteger("size");
        }
    }

    private ItemStack nextMode(EntityPlayer aPlayer, ItemStack aStack, ScanMode currentMode) {
        //Switch mode
        int nextMode = currentMode.ordinal() + 1;
        if (nextMode >= ScanMode.values().length) nextMode = 0;
        try {
            currentMode = ScanMode.values()[nextMode];
        } catch (ArrayIndexOutOfBoundsException e) {
            currentMode = ScanMode.NONE;
        }
        UT.NBT.makeInt(UT.NBT.getNBT(aStack), "mode", currentMode.ordinal());
        UT.Entities.sendchat(aPlayer, Chat.GOLD + "Changed The Mode to " + Chat.PURPLE + currentMode);
        return aStack;
    }

    private long getEnergy(MultiItem aItem, ItemStack aStack) {
        return aItem.getEnergyStored(EnergyType, aStack);
    }
}

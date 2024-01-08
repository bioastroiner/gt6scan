package bioast.mods.gt6mapper.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CartographyTableBlock extends Block {
    protected CartographyTableBlock() {
        super(Material.anvil);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return super.createTileEntity(world, metadata);
    }
}

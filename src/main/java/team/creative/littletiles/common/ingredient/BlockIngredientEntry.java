package team.creative.littletiles.common.ingredient;

import com.creativemd.creativecore.common.utils.mc.BlockUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BlockIngredientEntry {
    
    public Block block;
    public int meta;
    public double value;
    
    BlockIngredientEntry(Block block, int meta, double value) {
        this.block = block;
        this.meta = meta;
        this.value = value;
    }
    
    public ItemStack getItemStack() {
        return new ItemStack(block, 1, meta);
    }
    
    public ItemStack getTileItemStack() {
        ItemStack stack = new ItemStack(LittleTiles.blockTileNoTicking);
        NBTTagCompound nbt = new NBTTagCompound();
        new LittleVec(1, 1, 1).writeToNBT("size", nbt);
        
        LittleTile tile = new LittleTile(block, meta);
        tile.saveTileExtra(nbt);
        nbt.setString("tID", "BlockTileBlock");
        stack.setTagCompound(nbt);
        
        int count = (int) (value / LittleGridContext.get().pixelVolume);
        if (count == 0) {
            LittleGridContext.getMax().set(stack.getTagCompound());
            count = (int) (value / LittleGridContext.getMax().pixelVolume);
        }
        stack.setCount(count);
        return stack;
    }
    
    @Override
    public int hashCode() {
        return block.hashCode() + meta;
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof BlockIngredientEntry && ((BlockIngredientEntry) object).block == this.block && ((BlockIngredientEntry) object).meta == this.meta;
    }
    
    public IBlockState getState() {
        return BlockUtils.getState(block, meta);
    }
    
    public boolean is(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem()) == this.block && stack.getMetadata() == this.meta;
    }
    
    public BlockIngredientEntry copy() {
        return new BlockIngredientEntry(block, meta, value);
    }
    
    public BlockIngredientEntry copy(double value) {
        return new BlockIngredientEntry(this.block, this.meta, value);
    }
    
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setString("block", block.getRegistryName().toString());
        nbt.setInteger("meta", meta);
        nbt.setDouble("volume", value);
        return nbt;
    }
    
    public boolean isEmpty() {
        return value <= 0;
    }
    
    public void scale(int count) {
        value *= count;
    }
    
    public void scaleAdvanced(double scale) {
        this.value = (int) Math.ceil(this.value * scale);
    }
    
    @Override
    public String toString() {
        return "[" + block.getRegistryName() + "," + meta + "," + value + "]";
    }
    
}

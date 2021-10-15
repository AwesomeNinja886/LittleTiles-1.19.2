package team.creative.littletiles.common.gui;

import com.creativemd.creativecore.common.gui.container.SubContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubContainerConfigure extends SubContainer {
    
    public ItemStack stack;
    
    public SubContainerConfigure(EntityPlayer player, ItemStack stack) {
        super(player);
        this.stack = stack;
    }
    
    @Override
    public void createControls() {
        
    }
    
    @Override
    public void onPacketReceive(NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
    }
    
}

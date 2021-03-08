package com.creativemd.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.SubGuiHammer;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiGridSelector;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import com.creativemd.littletiles.common.api.IBoxSelector;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.tile.math.box.LittleBoxes;
import com.creativemd.littletiles.common.tile.math.vec.LittleAbsoluteVec;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.selector.TileSelector;
import com.creativemd.littletiles.common.util.shape.select.SelectShape;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleHammer extends Item implements IBoxSelector {
    
    private static boolean activeFilter = false;
    private static TileSelector currentFilter = null;
    
    public static boolean isFiltered() {
        return activeFilter;
    }
    
    public static void setFilter(boolean active, TileSelector filter) {
        activeFilter = active;
        currentFilter = filter;
    }
    
    public static TileSelector getFilter() {
        return currentFilter;
    }
    
    public ItemLittleHammer() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
        setMaxStackSize(1);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("can be used to chisel blocks");
        SelectShape shape = getShape(stack);
        tooltip.add("mode: " + shape.key);
        shape.addExtraInformation(worldIn, stack.getTagCompound(), tooltip, getContext(stack));
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.OFF_HAND)
            return new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand));
        if (!world.isRemote)
            GuiHandler.openGuiItem(player, world);
        return new ActionResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
    
    @Override
    public LittleBoxes getBox(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleAbsoluteVec absoluteHit) {
        SelectShape shape = getShape(stack);
        
        return shape.getHighlightBoxes(world, result.getBlockPos(), player, stack.getTagCompound(), result, getContext(stack));
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onClickBlock(World world, ItemStack stack, EntityPlayer player, RayTraceResult result, LittleAbsoluteVec absoluteHit) {
        SelectShape shape = getShape(stack);
        if (shape.leftClick(player, stack.getTagCompound(), result, getContext(stack)))
            if (isFiltered())
                new LittleActionDestroyBoxesFiltered(shape.getBoxes(world, result.getBlockPos(), player, stack.getTagCompound(), result, getContext(stack)), getFilter()).execute();
            else
                new LittleActionDestroyBoxes(shape.getBoxes(world, result.getBlockPos(), player, stack.getTagCompound(), result, getContext(stack))).execute();
        return true;
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0F;
    }
    
    @Override
    public void onDeselect(World world, ItemStack stack, EntityPlayer player) {
        getShape(stack).deselect(player, stack.getTagCompound(), getContext(stack));
    }
    
    @Override
    public boolean hasCustomBox(World world, ItemStack stack, EntityPlayer player, IBlockState state, RayTraceResult result, LittleAbsoluteVec absoluteHit) {
        return LittleAction.isBlockValid(state) || world.getTileEntity(result.getBlockPos()) instanceof TileEntityLittleTiles;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        return new SubGuiHammer(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerConfigure(player, stack);
    }
    
    @Override
    public void rotateLittlePreview(ItemStack stack, Rotation rotation) {
        SelectShape shape = getShape(stack);
        if (shape != null)
            shape.rotate(rotation, stack.getTagCompound());
    }
    
    @Override
    public void flipLittlePreview(ItemStack stack, Axis axis) {
        SelectShape shape = getShape(stack);
        if (shape != null)
            shape.flip(axis, stack.getTagCompound());
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiGridSelector(stack, ItemMultiTiles.currentContext, isFiltered(), getFilter()) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, boolean activeFilter, TileSelector selector) {
                setFilter(activeFilter, selector);
                ItemMultiTiles.currentContext = context;
            }
        };
    }
    
    public static SelectShape getShape(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        return SelectShape.getShape(stack.getTagCompound().getString("shape"));
    }
    
    @Override
    public LittleGridContext getContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
}

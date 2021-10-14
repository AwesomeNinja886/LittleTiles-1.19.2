package team.creative.littletiles.common.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.client.gui.SubGuiRecipe;
import com.creativemd.littletiles.client.gui.SubGuiRecipeAdvancedSelection;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.client.render.cache.ItemModelCache;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.container.SubContainerRecipeAdvanced;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.selection.mode.SelectionMode;
import com.creativemd.littletiles.common.util.tooltip.IItemTooltip;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.client.render.model.ICreativeRendered;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.packet.LittleBlockPacket;
import team.creative.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import team.creative.littletiles.common.packet.LittleSelectionModePacket;
import team.creative.littletiles.common.placement.PlacementPosition;
import team.creative.littletiles.common.placement.mode.PlacementMode;

public class ItemLittleRecipeAdvanced extends Item implements ILittlePlacer, ICreativeRendered, IItemTooltip {
    
    public ItemLittleRecipeAdvanced() {
        setCreativeTab(LittleTiles.littleTab);
        hasSubtypes = true;
    }
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("structure") && stack.getTagCompound().getCompoundTag("structure").hasKey("name"))
            return stack.getTagCompound().getCompoundTag("structure").getString("name");
        return super.getItemStackDisplayName(stack);
    }
    
    @Override
    public boolean hasLittlePreview(ItemStack stack) {
        return stack.hasTagCompound() && (stack.getTagCompound().getInteger("count") > 0 || stack.getTagCompound().hasKey("children"));
    }
    
    @Override
    public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
        LittlePreview.savePreview(previews, stack);
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack) {
        return LittlePreview.getPreview(stack);
    }
    
    @Override
    public LittlePreviews getLittlePreview(ItemStack stack, boolean allowLowResolution) {
        return LittlePreview.getPreview(stack, allowLowResolution);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
        if (!((ItemLittleRecipeAdvanced) stack.getItem()).hasLittlePreview(stack))
            return new SubGuiRecipeAdvancedSelection(stack);
        return new SubGuiRecipe(stack);
    }
    
    @Override
    public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
        return new SubContainerRecipeAdvanced(player, stack);
    }
    
    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return 0F;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        
    }
    
    @Override
    public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        IBlockState state = world.getBlockState(result.getBlockPos());
        if (state.getBlock() instanceof BlockTile) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("secondMode", LittleAction.isUsingSecondMode(player));
            PacketHandler.sendPacketToServer(new LittleBlockPacket(world, result.getBlockPos(), player, BlockPacketAction.RECIPE, nbt));
            return true;
        }
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<RenderBox> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().getInteger("count") > 0)
            return LittlePreview.getCubesForStackRendering(stack);
        return new ArrayList<RenderBox>();
    }
    
    @SideOnly(Side.CLIENT)
    public static IBakedModel model;
    
    @Override
    @SideOnly(Side.CLIENT)
    public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();
        
        if (cameraTransformType == TransformType.GUI || !stack.hasTagCompound() || !stack.getTagCompound().hasKey("tiles")) {
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.disableDepth();
            if (model == null)
                model = mc.getRenderItem().getItemModelMesher().getModelManager()
                        .getModel(new ModelResourceLocation(LittleTiles.modid + ":recipeadvanced_background", "inventory"));
            ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
            
            mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
            
            if (cameraTransformType == TransformType.GUI)
                GlStateManager.enableDepth();
        }
        GlStateManager.popMatrix();
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void saveCachedModel(EnumFacing facing, BlockRenderLayer layer, List<BakedQuad> cachedQuads, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
        ItemModelCache.cacheModel(stack, facing, cachedQuads);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public List<BakedQuad> getCachedModel(EnumFacing facing, BlockRenderLayer layer, IBlockState state, TileEntity te, ItemStack stack, boolean threaded) {
        return ItemModelCache.requestCache(stack, facing);
    }
    
    @Override
    public boolean containsIngredients(ItemStack stack) {
        return false;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (hasLittlePreview(stack))
            return true;
        getSelectionMode(stack).onRightClick(player, stack, result.getBlockPos());
        PacketHandler.sendPacketToServer(new LittleSelectionModePacket(result.getBlockPos(), true));
        return true;
    }
    
    @Override
    public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
        if (hasLittlePreview(stack))
            return true;
        getSelectionMode(stack).onLeftClick(player, stack, result.getBlockPos());
        PacketHandler.sendPacketToServer(new LittleSelectionModePacket(result.getBlockPos(), false));
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
        return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
            
            @Override
            public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
                ItemMultiTiles.currentContext = context;
                ItemMultiTiles.currentMode = mode;
            }
            
        };
    }
    
    @Override
    public PlacementMode getPlacementMode(ItemStack stack) {
        if (!ItemMultiTiles.currentMode.canPlaceStructures() && stack.hasTagCompound() && stack.getTagCompound().hasKey("structure"))
            return PlacementMode.getStructureDefault();
        return ItemMultiTiles.currentMode;
    }
    
    @Override
    public LittleGridContext getPositionContext(ItemStack stack) {
        return ItemMultiTiles.currentContext;
    }
    
    @Override
    public LittleVec getCachedSize(ItemStack stack) {
        if (stack.getTagCompound().hasKey("size"))
            return LittlePreview.getSize(stack);
        return null;
    }
    
    @Override
    public LittleVec getCachedOffset(ItemStack stack) {
        return LittlePreview.getOffset(stack);
    }
    
    @Override
    public Object[] tooltipData(ItemStack stack) {
        return new Object[] { Minecraft.getMinecraft().gameSettings.keyBindAttack.getDisplayName(), Minecraft.getMinecraft().gameSettings.keyBindUseItem
                .getDisplayName(), Minecraft.getMinecraft().gameSettings.keyBindPickBlock.getDisplayName(), LittleTilesClient.configure.getDisplayName() };
    }
    
    public static SelectionMode getSelectionMode(ItemStack stack) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        return SelectionMode.getOrDefault(stack.getTagCompound().getString("selmode"));
    }
    
    public static void setSelectionMode(ItemStack stack, SelectionMode mode) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        stack.getTagCompound().setString("selmode", mode.name);
    }
    
    public static boolean isRecipe(Item item) {
        return item instanceof ItemLittleRecipe || item instanceof ItemLittleRecipeAdvanced;
    }
}

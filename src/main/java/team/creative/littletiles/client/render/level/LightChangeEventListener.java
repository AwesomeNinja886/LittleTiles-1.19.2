package team.creative.littletiles.client.render.level;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littletiles.common.block.mc.BlockTile;

@SideOnly(Side.CLIENT)
public class LightChangeEventListener implements IWorldEventListener {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    
    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
        
    }
    
    @Override
    public void notifyLightSet(BlockPos pos) {
        TileEntityLittleTiles te = BlockTile.loadTe(mc.world, pos);
        if (te != null)
            te.render.hasLightChanged = true;
    }
    
    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        
    }
    
    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        
    }
    
    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {
        
    }
    
    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
        
    }
    
    @Override
    public void spawnParticle(int p_190570_1_, boolean p_190570_2_, boolean p_190570_3_, double p_190570_4_, double p_190570_6_, double p_190570_8_, double p_190570_10_, double p_190570_12_, double p_190570_14_, int... p_190570_16_) {
        
    }
    
    @Override
    public void onEntityAdded(Entity entityIn) {
        
    }
    
    @Override
    public void onEntityRemoved(Entity entityIn) {
        
    }
    
    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
        
    }
    
    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
        
    }
    
    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        
    }
    
}

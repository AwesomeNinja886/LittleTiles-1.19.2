package com.creativemd.littletiles.common.structure.type.premade.signal;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.common.utils.math.BooleanUtils;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.math.box.AlignedBox;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.signal.component.ISignalStructureComponent;
import com.creativemd.littletiles.common.structure.signal.component.SignalComponentType;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewFacing;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.vec.SurroundingBox;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleSignalOutput extends LittleSignalCableBase implements ISignalStructureComponent {
    
    private final boolean[] state;
    @StructureDirectional
    public EnumFacing facing;
    
    public LittleSignalOutput(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
        this.state = new boolean[getBandwidth()];
    }
    
    @Override
    public boolean canConnect(EnumFacing facing) {
        return facing == this.facing;
    }
    
    @Override
    public boolean[] getState() {
        return state;
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        super.loadFromNBTExtra(nbt);
        BooleanUtils.intToBool(nbt.getInteger("state"), state);
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        super.writeToNBTExtra(nbt);
        nbt.setInteger("state", BooleanUtils.boolToInt(state));
    }
    
    @Override
    public SignalComponentType getType() {
        return SignalComponentType.OUTPUT;
    }
    
    @Override
    public void changed() {
        findNetwork().update();
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void renderFace(EnumFacing facing, LittleGridContext context, LittleBox renderBox, int distance, Axis axis, Axis one, Axis two, boolean positive, boolean oneSidedRenderer, List<LittleRenderBox> cubes) {
        super.renderFace(facing, context, renderBox.copy(), distance, axis, one, two, positive, oneSidedRenderer, cubes);
        
        LittleRenderBox cube = renderBox.getRenderingCube(context, LittleTiles.outputArrow, facing.ordinal());
        //cube.color = color;
        cube.keepVU = true;
        cube.allowOverlap = true;
        
        if (positive) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - (float) context.toVanillaGrid(renderBox.getSize(axis)) * 0.7F);
        }
        float shrink = 0.14F;
        float shrinkOne = cube.getSize(one) * shrink;
        float shrinkTwo = cube.getSize(two) * shrink;
        cube.setMin(one, cube.getMin(one) + shrinkOne);
        cube.setMax(one, cube.getMax(one) - shrinkOne);
        cube.setMin(two, cube.getMin(two) + shrinkTwo);
        cube.setMax(two, cube.getMax(two) - shrinkTwo);
        cubes.add(cube);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void render(SurroundingBox box, LittleBox overallBox, List<LittleRenderBox> cubes) {
        super.render(box, overallBox, cubes);
        
        AlignedBox structureBox = new AlignedBox(overallBox.getBox(box.getContext()));
        LittleRenderBox block = (LittleRenderBox) new LittleRenderBox(structureBox, null, LittleTiles.dyeableBlock, 0).setColor(color);
        block.allowOverlap = true;
        cubes.add(block);
        
        LittleRenderBox cube = new LittleRenderBox(structureBox, null, LittleTiles.dyeableBlock, 0);
        cube.setColor(ColorUtils.ORANGE);
        
        Axis axis = facing.getAxis();
        
        float thickness = cube.getSize(axis) * 0.25F;
        float sizePercentage = 0.25F;
        
        Axis one = RotationUtils.getOne(axis);
        Axis two = RotationUtils.getTwo(axis);
        
        float sizeOne = cube.getSize(one);
        cube.setMin(one, cube.getMin(one) + sizeOne * sizePercentage);
        cube.setMax(one, cube.getMax(one) - sizeOne * sizePercentage);
        
        float sizeTwo = cube.getSize(two);
        cube.setMin(two, cube.getMin(two) + sizeTwo * sizePercentage);
        cube.setMax(two, cube.getMax(two) - sizeTwo * sizePercentage);
        
        if (facing.getAxisDirection() == AxisDirection.POSITIVE) {
            cube.setMin(axis, cube.getMax(axis));
            cube.setMax(axis, cube.getMax(axis) + thickness);
        } else {
            cube.setMax(axis, cube.getMin(axis));
            cube.setMin(axis, cube.getMin(axis) - thickness);
        }
        cubes.add(cube);
    }
    
    @Override
    public int getIndex(EnumFacing facing) {
        return 0;
    }
    
    @Override
    public EnumFacing getFacing(int index) {
        return facing;
    }
    
    @Override
    public int getId() {
        return getParent().childId;
    }
    
    @Override
    public LittleStructure getStructure() {
        try {
            return getParent().getStructure();
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public String info() {
        if (getParent() != null)
            return "o" + getId() + ":" + BooleanUtils.print(getState());
        return "";
    }
    
    public static class LittleStructureTypeOutput extends LittleStructureTypeNetwork {
        
        public LittleStructureTypeOutput(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid, int bandwidth) {
            super(id, category, structureClass, attribute, modid, bandwidth, 1);
        }
        
        @Override
        public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
            List<PlacePreview> result = super.getSpecialTiles(previews);
            EnumFacing facing = (EnumFacing) loadDirectional(previews, "facing");
            LittleBox box = previews.getSurroundingBox();
            result.add(new PlacePreviewFacing(box, facing, ColorUtils.ORANGE));
            return result;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public List<RenderBox> getRenderingCubes(LittlePreviews previews) {
            List<RenderBox> cubes = new ArrayList<>();
            float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F + 0.05) * 1.4);
            cubes = new ArrayList<>();
            cubes.add(new RenderBox(0, 0.5F - size, 0.5F - size, size * 2, 0.5F + size, 0.5F + size, LittleTiles.dyeableBlock).setColor(getColor(previews)));
            cubes.add(new RenderBox(size * 2, 0.5F - size, 0.5F - size, size * 2.5F, 0.5F + size, 0.5F + size, LittleTiles.dyeableBlock).setColor(ColorUtils.ORANGE));
            return cubes;
        }
        
        @Override
        public int getBandwidth() {
            return bandwidth;
        }
        
        @Override
        public void changed() {
            
        }
        
        @Override
        public boolean[] getState() {
            return null;
        }
        
        @Override
        public SignalComponentType getType() {
            return SignalComponentType.OUTPUT;
        }
        
    }
    
}

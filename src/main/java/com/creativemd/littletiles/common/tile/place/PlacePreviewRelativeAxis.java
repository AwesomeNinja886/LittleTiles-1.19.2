package com.creativemd.littletiles.common.tile.place;

import java.util.List;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.EnumFacing.Axis;
import team.creative.littletiles.common.structure.relative.StructureRelative;

public class PlacePreviewRelativeAxis extends PlacePreviewRelative {
    
    public Axis axis;
    
    public PlacePreviewRelativeAxis(LittleBox box, StructureRelative relative, StructureDirectionalField relativeType, Axis axis) {
        super(box, relative, relativeType);
        this.axis = axis;
    }
    
    @Override
    public List<LittleRenderBox> getPreviews(LittleGridContext context) {
        List<LittleRenderBox> cubes = super.getPreviews(context);
        LittleRenderBox cube = cubes.get(0);
        int max = 40 * context.size;
        int min = -max;
        switch (axis) {
        case X:
            cube.minX = min;
            cube.maxX = max;
            break;
        case Y:
            cube.minY = min;
            cube.maxY = max;
            break;
        case Z:
            cube.minZ = min;
            cube.maxZ = max;
            break;
        default:
            break;
        }
        cubes.add(cube);
        return cubes;
    }
}

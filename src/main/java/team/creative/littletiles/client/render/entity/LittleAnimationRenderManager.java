package team.creative.littletiles.client.render.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.map.ChunkLayerMap;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.cache.pipeline.LittleRenderPipelineType;
import team.creative.littletiles.client.render.mc.RebuildTaskExtender;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;

@OnlyIn(Dist.CLIENT)
public class LittleAnimationRenderManager extends LittleEntityRenderManager<LittleAnimationEntity> implements RenderChunkExtender {
    
    private final ChunkLayerMap<VertexBuffer> buffers = new ChunkLayerMap<>();
    private final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
    private List<BlockEntity> renderableBlockEntities = new ArrayList<>();
    private BufferBuilder.SortState transparencyState;
    private boolean needsUpdate = false;
    
    public LittleAnimationRenderManager(LittleAnimationEntity entity) {
        super(entity);
    }
    
    @Override
    public RenderChunkExtender getRenderChunk(BlockPos pos) {
        return this;
    }
    
    @Override
    public LittleAnimationLevel getLevel() {
        return (LittleAnimationLevel) super.getLevel();
    }
    
    @Override
    public void begin(BufferBuilder builder) {
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
    }
    
    @Override
    public VertexBuffer getVertexBuffer(RenderType layer) {
        VertexBuffer buffer = this.buffers.get(layer);
        if (buffer == null)
            this.buffers.put(layer, buffer = new VertexBuffer(VertexBuffer.Usage.STATIC));
        return buffer;
    }
    
    @Override
    public void markReadyForUpdate(boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void compileChunks(Camera camera) {
        if (needsUpdate) {
            needsUpdate = false;
            hasBlocks.clear();
            renderableBlockEntities.clear();
            RebuildTask rebuild = new RebuildTask();
            Vec3 cam = camera.getPosition();
            CompileResults results = rebuild.compile((float) cam.x, (float) cam.y, (float) cam.z, LittleTilesClient.ANIMATION_HANDLER.fixedBuffers);
            globalBlockEntities.clear();
            globalBlockEntities.addAll(results.globalBlockEntities);
            renderableBlockEntities = results.blockEntities;
            transparencyState = results.transparencyState;
            results.renderedLayers.forEach((layer, rendered) -> {
                VertexBuffer buffer = getVertexBuffer(layer);
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(rendered);
                    VertexBuffer.unbind();
                }
                hasBlocks.add(layer);
            });
            hasBlocks.addAll(results.renderedLayers.keySet());
        }
    }
    
    @Override
    protected void renderAllBlockEntities(PoseStack pose, Frustum frustum, Vec3 cam, float frameTime, MultiBufferSource bufferSource) {
        if (renderableBlockEntities != null)
            for (BlockEntity blockEntity : renderableBlockEntities)
                renderBlockEntity(blockEntity, pose, frustum, cam, frameTime, bufferSource);
    }
    
    @Override
    public void prepareModelOffset(MutableBlockPos modelOffset, BlockPos pos) {
        modelOffset.set(pos.getX(), pos.getY(), pos.getZ());
    }
    
    @Override
    public BlockPos standardOffset() {
        return BlockPos.ZERO;
    }
    
    @Override
    public SortState getTransparencyState() {
        return transparencyState;
    }
    
    @Override
    public boolean isEmpty(RenderType layer) {
        return !hasBlocks.contains(layer);
    }
    
    @Override
    public void setHasBlock(RenderType layer) {
        hasBlocks.add(layer);
    }
    
    @Override
    public void setQuadSorting(BufferBuilder builder, double x, double y, double z) {
        builder.setQuadSorting(VertexSorting.byDistance((float) x, (float) y, (float) z));
    }
    
    @Override
    public void resortTransparency(RenderType layer, double x, double y, double z) {
        if (transparencyState != null && hasBlocks.contains(RenderType.translucent())) {
            BufferBuilder bufferbuilder = LittleTilesClient.ANIMATION_HANDLER.fixedBuffers.builder(RenderType.translucent());
            begin(bufferbuilder);
            bufferbuilder.restoreSortState(transparencyState);
            setQuadSorting(bufferbuilder, x, y, z);
            transparencyState = bufferbuilder.getSortState();
            BufferBuilder.RenderedBuffer rendered = bufferbuilder.end();
            VertexBuffer buffer = getVertexBuffer(layer);
            if (!buffer.isInvalid()) {
                buffer.bind();
                buffer.upload(rendered);
                VertexBuffer.unbind();
            }
        }
    }
    
    @Override
    public void renderChunkLayer(RenderType layer, PoseStack pose, double x, double y, double z, Matrix4f projectionMatrix, Uniform offset) {
        if (hasBlocks.contains(layer)) {
            VertexBuffer vertexbuffer = buffers.get(layer);
            if (vertexbuffer == null)
                return;
            if (offset != null) {
                offset.set((float) -x, (float) -y, (float) -z);
                offset.upload();
            }
            
            vertexbuffer.bind();
            vertexbuffer.draw();
        }
        
        if (offset != null)
            offset.set(0F, 0F, 0F);
    }
    
    @Override
    protected void setSectionDirty(int x, int y, int z, boolean playerChanged) {
        needsUpdate = true;
    }
    
    @Override
    public void unload() {
        super.unload();
        this.buffers.forEach(VertexBuffer::close);
    }
    
    @Override
    public LittleRenderPipelineType getPipeline() {
        return LittleRenderPipelineType.FORGE;
    }
    
    static final class CompileResults {
        
        public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
        public final List<BlockEntity> blockEntities = new ArrayList<>();
        public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
        @Nullable
        public BufferBuilder.SortState transparencyState;
        
        public boolean isEmpty() {
            return renderedLayers.isEmpty() && globalBlockEntities.isEmpty() && blockEntities.isEmpty();
        }
    }
    
    private class RebuildTask implements RebuildTaskExtender {
        
        private ChunkLayerMap<ChunkLayerCache> caches;
        private ChunkBufferBuilderPack pack;
        private Set<RenderType> renderTypes;
        
        private CompileResults compile(float x, float y, float z, ChunkBufferBuilderPack pack) {
            this.pack = pack;
            
            CompileResults results = new CompileResults();
            LittleRenderPipelineType.startCompile(LittleAnimationRenderManager.this, this);
            
            renderTypes = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
            
            for (BETiles block : getLevel())
                handleBlockEntity(results, block);
            
            if (renderTypes.contains(RenderType.translucent())) {
                BufferBuilder builder = pack.builder(RenderType.translucent());
                if (!builder.isCurrentBatchEmpty()) {
                    setQuadSorting(builder, x, y, z);
                    results.transparencyState = builder.getSortState();
                }
            }
            
            for (RenderType layer : renderTypes) {
                BufferBuilder.RenderedBuffer rendered = pack.builder(layer).endOrDiscardIfEmpty();
                if (rendered != null)
                    results.renderedLayers.put(layer, rendered);
            }
            
            LittleRenderPipelineType.endCompile(LittleAnimationRenderManager.this, this);
            return results;
        }
        
        private void handleBlockEntity(CompileResults results, BETiles entity) {
            LittleRenderPipelineType.compile(LittleAnimationRenderManager.this, entity, this);
            BlockEntityRenderer blockentityrenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
            if (blockentityrenderer != null)
                if (blockentityrenderer.shouldRenderOffScreen(entity))
                    results.globalBlockEntities.add(entity);
                else
                    results.blockEntities.add(entity); //FORGE: Fix MC-112730
        }
        
        @Override
        public BufferBuilder builder(RenderType layer) {
            BufferBuilder builder = pack.builder(layer);
            if (renderTypes.add(layer))
                LittleAnimationRenderManager.this.begin(builder);
            return builder;
        }
        
        @Override
        public ChunkLayerMap<ChunkLayerCache> getLayeredCache() {
            return caches;
        }
        
        @Override
        public ChunkLayerCache getOrCreate(RenderType layer) {
            if (caches == null)
                caches = new ChunkLayerMap<>();
            ChunkLayerCache cache = caches.get(layer);
            if (cache == null)
                caches.put(layer, cache = new ChunkLayerCache());
            return cache;
        }
        
        @Override
        public void clear() {
            this.pack = null;
            this.renderTypes = null;
        }
        
    }
    
}

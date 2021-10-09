package com.creativemd.littletiles.common.structure;

import team.creative.littletiles.common.entity.EntityAnimation;

public interface IAnimatedStructure {
    
    public void setAnimation(EntityAnimation animation);
    
    public EntityAnimation getAnimation();
    
    public boolean isInMotion();
    
    public boolean isAnimated();
    
    public void destroyAnimation();
    
}

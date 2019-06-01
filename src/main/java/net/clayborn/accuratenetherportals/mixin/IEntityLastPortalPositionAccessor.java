package net.clayborn.accuratenetherportals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(Entity.class)
public interface IEntityLastPortalPositionAccessor {

	@Accessor
	BlockPos getLastPortalPosition();	
	
}

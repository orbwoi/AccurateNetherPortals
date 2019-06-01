package net.clayborn.accuratenetherportals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.clayborn.accuratenetherportals.IEntityMixinLastPortalFrontTopLeftAccessor;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityMixinLastPortalFrontTopLeftAccessor {

	private BlockPos accuratenetherportals_lastPortalFrontTopLeft;
	
	// Mixin to capture the 'FROM' portal FrontTopLeft
	@Inject (method = "setInPortal",
			 locals = LocalCapture.CAPTURE_FAILHARD,
			 at = @At(value = "INVOKE_ASSIGN", 
			          target = "net/minecraft/block/PortalBlock.findPortal(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/pattern/BlockPattern$Result;"))
	public void onFindPortal(BlockPos blockPos_1, CallbackInfo i, BlockPattern.Result result) {
		if (result != null)
		{
			accuratenetherportals_lastPortalFrontTopLeft = result.getFrontTopLeft();
		}
    }
	
	@Override
	public BlockPos accuratenetherportals_getLastPortalFrontTopLeft()
	{
		return accuratenetherportals_lastPortalFrontTopLeft;
	}
}

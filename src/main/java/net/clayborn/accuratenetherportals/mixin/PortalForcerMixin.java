package net.clayborn.accuratenetherportals.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.raphydaphy.crochet.data.PlayerData;

import net.clayborn.accuratenetherportals.AccurateNetherPortalsMod;
import net.clayborn.accuratenetherportals.IEntityMixinLastPortalFrontTopLeftAccessor;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PortalForcer;

@Mixin(PortalForcer.class)
public abstract class PortalForcerMixin {
	
	final static private String PORTALHISTORYTAG = "portalhistory";
	
	@Inject(at = @At("HEAD"), method = "usePortal")
	private void usePortal(Entity entity_1, float float_1, CallbackInfoReturnable<Boolean> info) {
		//System.out.println("[DEBUG] usePortal called!");
		
		// we only support players for now
		if (entity_1 != null && entity_1 instanceof ServerPlayerEntity)
		{
			// Here we need to re-direction the teleportation as needed to make portals not stupid
			
			// First, find out what portal we are coming from
			BlockPos lastFromPortalFrontTopLeft = ((IEntityMixinLastPortalFrontTopLeftAccessor)(Object)entity_1).accuratenetherportals_getLastPortalFrontTopLeft();
			
			// safety checks
			if (lastFromPortalFrontTopLeft == null && entity_1.dimension != null) return;
			
			// note: entity dim has already been updated to match TO not from dimension at this point
			String cacheName = PORTALHISTORYTAG + "." +
			                   String.valueOf(entity_1.dimension.getRawId() == 0 ? -1 : 0) + "." +
			                   String.valueOf(lastFromPortalFrontTopLeft.getX()) + "." +
			                   String.valueOf(lastFromPortalFrontTopLeft.getY()) + "." +
			                   String.valueOf(lastFromPortalFrontTopLeft.getZ());
			
			CompoundTag playerData = PlayerData.get((PlayerEntity) entity_1, AccurateNetherPortalsMod.MODID);
			
			// Next, see if that portal has a cached exit for this entity
			if (playerData != null && playerData.containsKey(cacheName))
			{
				int[] newtarget = playerData.getIntArray(cacheName);
				if (newtarget.length != 3)
				{
					// corrupt NBT data!
					System.out.println("[WARNING] Player found with corrupt NBT portal history data, removing bad entry...");
					
					// kill the bad data
					playerData.remove(cacheName);
					PlayerData.markDirty((PlayerEntity) entity_1);
					return;
				}
				
				// Update entity position to fix the target
				entity_1.x  = newtarget[0];
				entity_1.y  = newtarget[1];
				entity_1.z  = newtarget[2];
			}
		
		}
		
	}	
	
	private BlockPos lastToPortalFrontTopLeft;	
	
	// Mixin to capture the 'TO' portal FrontTopLeft
	@Inject(method = "getPortal",
			locals = LocalCapture.CAPTURE_FAILHARD,
		    at = @At(value = "INVOKE_ASSIGN", 
	                 target = "net/minecraft/block/PortalBlock.findPortal(Lnet/minecraft/world/IWorld;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/pattern/BlockPattern$Result;"))
	private void onFindPortal(BlockPos blockPos_1, Vec3d vec3d_1, Direction direction_1, double double_1, double double_2, boolean boolean_1, CallbackInfoReturnable<BlockPattern.TeleportTarget> cir, int int_1, boolean boolean_2, BlockPos blockPos_2, BlockPattern.Result result) {
		//System.out.println("[DEBUG] getPortal called findPortal!");
		
		// since we are single threaded (lol minecraft), we can just cache it and use it in the parent function
		if (result != null) {
			lastToPortalFrontTopLeft = result.getFrontTopLeft();
		}
	}
		
	// Method to record nbt portal history for ServerPlayerEntity	
	@Inject(method = "usePortal",
		    at = @At(value = "INVOKE_ASSIGN", 
	                 target = "net/minecraft/world/PortalForcer.getPortal(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Direction;DDZ)Lnet/minecraft/block/pattern/BlockPattern$TeleportTarget;"))
	private void onGetPortal(Entity entity_1, float float_1, CallbackInfoReturnable<Boolean> info) {
		//System.out.println("[DEBUG] usePortal called getPortal!");
		
		// we only support players for now
		if (entity_1 != null && entity_1 instanceof ServerPlayerEntity)
		{
			// obtain the from position in the from dim so we can record where to teleport back later
			BlockPos fromPos = ((IEntityLastPortalPositionAccessor)entity_1).getLastPortalPosition();
			
			// safety checks
			if (fromPos == null || lastToPortalFrontTopLeft == null || entity_1.dimension == null) return;
			
			int[] fromPosArray = { fromPos.getX(), fromPos.getY(), fromPos.getZ() };
			
			// prepare to write the history
			String cacheName = PORTALHISTORYTAG + "." +
	                   String.valueOf(entity_1.dimension.getRawId()) + "." +
	                   String.valueOf(lastToPortalFrontTopLeft.getX()) + "." +
	                   String.valueOf(lastToPortalFrontTopLeft.getY()) + "." +
	                   String.valueOf(lastToPortalFrontTopLeft.getZ());
			
			CompoundTag playerData = PlayerData.get((PlayerEntity) entity_1, AccurateNetherPortalsMod.MODID);
			
			if (playerData != null)
			{
				//System.out.println("[DEBUG] writing nbt portal history of " + cacheName);
			
				// write the history item
				playerData.putIntArray(cacheName, fromPosArray);
				PlayerData.markDirty((PlayerEntity) entity_1);
			}
		}
	}
	
	
}
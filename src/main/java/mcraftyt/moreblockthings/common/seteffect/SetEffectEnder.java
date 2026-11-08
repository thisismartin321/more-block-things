package mcraftyt.moreblockthings.common.seteffect;

import java.util.LinkedHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Teleports the player up to 30 blocks in the viewing direction with the key (only onto suitable, free spots) */
public class SetEffectEnder extends SetEffect {

	protected SetEffectEnder() {
		super();
		this.color = DARK_PURPLE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player))
			return;
		float maxYawOffset = 6f;
		float yawIncrement = 2f;
		float rollIncrement = (float) (Math.PI / 4f);
		int distance = 30;
		int maxYOffset = 3;
		// small -> large deviation of the viewing direction (ring around the viewing direction)
		for (float yawOffset = 0; yawOffset <= maxYawOffset; yawOffset += yawIncrement)
			for (float roll = 0; roll <= 2 * Math.PI; roll += (yawOffset == 0 ? 999 : rollIncrement)) {
				HitResult result = pick(player, distance, yawOffset, roll);
				if (result.getType() != HitResult.Type.BLOCK)
					continue;
				// try the hit position and the one above it first
				LinkedHashSet<BlockPos> positions = new LinkedHashSet<>();
				BlockPos resultPos = BlockPos.containing(result.getLocation());
				positions.add(resultPos);
				positions.add(resultPos.above());
				for (BlockPos pos : positions)
					if (attemptTeleport(player, pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d, maxYOffset)) {
						if (player.isPassenger())
							player.stopRiding();
						playSound(player.level(), player.blockPosition(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
						player.fallDistance = 0;
						setCooldown(player, 30);
						damageArmor(player, 2, false);
						return;
					}
			}
		// no valid spot found
		playSound(player.level(), player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 1.0F,
				player.level().getRandom().nextFloat() + 0.5F);
		setCooldown(player, 10);
	}

	/** Like LivingEntity#attemptTeleport, but stricter about height (at most maxYOffset blocks of air down to the ground) */
	private boolean attemptTeleport(Player player, double x, double y, double z, int maxYOffset) {
		double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
		double targetY = y;
		boolean success = false;
		BlockPos pos = BlockPos.containing(x, y, z);
		Level level = player.level();
		if (level.isLoaded(pos)) {
			boolean foundGround = false;
			while (!foundGround && pos.getY() > level.getMinY() && maxYOffset-- > 0) {
				BlockPos below = pos.below();
				if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty()) { // solid ground (collision shape present)
					foundGround = true;
				} else {
					--targetY;
					pos = below;
				}
			}
			if (foundGround) {
				player.teleportTo(x, targetY, z);
				if (level.noCollision(player) && !level.containsAnyLiquid(player.getBoundingBox()))
					success = true;
			}
		}
		if (!success) {
			player.teleportTo(oldX, oldY, oldZ);
			return false;
		}
		level.broadcastEntityEvent(player, (byte) 46); // particles
		return true;
	}

	/** Ray in the viewing direction, slightly rotated (ring around the viewing direction) */
	private HitResult pick(Player player, double distance, float yawOffset, float roll) {
		Vec3 start = player.getEyePosition(0);
		Vec3 look = directionVector(player.getXRot() + Mth.sin(roll) * yawOffset, player.getYHeadRot() + Mth.cos(roll) * yawOffset);
		Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);
		return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
	}

	/** Like Entity#getVectorForRotation (protected there) */
	private static Vec3 directionVector(float pitch, float yaw) {
		float f = pitch * ((float) Math.PI / 180F);
		float f1 = -yaw * ((float) Math.PI / 180F);
		float f2 = Mth.cos(f1);
		float f3 = Mth.sin(f1);
		float f4 = Mth.cos(f);
		float f5 = Mth.sin(f);
		return new Vec3(f3 * f4, -f5, f2 * f4);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "end_stone", "end_bricks", "ender", "endstone", "chorus", "purpur", "teleport");
	}

}

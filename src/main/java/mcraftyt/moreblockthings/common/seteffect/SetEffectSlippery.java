package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** On smooth blocks speed is retained more strongly (client-side, as in the original) */
public class SetEffectSlippery extends SetEffect {

	protected SetEffectSlippery() {
		super();
		this.color = AQUA;
	}

	@Override
	public void onClientTick(Player player) {
		if (player.onGround() && player.zza == 0 && player.xxa == 0) {
			Block block = player.level().getBlockState(player.blockPosition().below()).getBlock();
			if (!(block instanceof AirBlock) && block.getFriction() <= 0.6f) {
				double factor = player.isInWater() ? 1.2d : 1.6d;
				Vec3 motion = player.getDeltaMovement();
				double x = Math.abs(motion.x()) < 0.4d ? motion.x() * factor : motion.x();
				double z = Math.abs(motion.z()) < 0.4d ? motion.z() * factor : motion.z();
				player.setDeltaMovement(new Vec3(x, motion.y(), z));
			}
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "waxed") || (block.getFriction() > 0.6f && !registryNameContains(block, "slime"));
	}

}

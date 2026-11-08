package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoulSandBlock;
import net.minecraft.world.phys.Vec3;

/** Slower, no fall damage when not sneaking, soul particles */
public class SetEffectSlow_Motion extends SetEffect {

	protected SetEffectSlow_Motion() {
		super();
		this.color = GRAY;
		this.potionEffects.add(new MobEffectInstance(MobEffects.SLOWNESS, 10, 2, true, false));
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!player.isShiftKeyDown())
			player.fallDistance = 0;
	}

	@Override
	public void onClientTick(Player player) {
		Vec3 motion = player.getDeltaMovement();
		if (player.level().getRandom().nextInt(10) == 0 && (motion.x != 0 || motion.z != 0 || !player.onGround()))
			player.level().addParticle(ParticleTypes.SOUL, player.getX() + player.level().getRandom().nextDouble() - 0.5D,
					player.getY() + player.level().getRandom().nextDouble() + 0.3d,
					player.getZ() + player.level().getRandom().nextDouble() - 0.5D, 0, 0, 0);
		if (!player.isShiftKeyDown())
			// no Y thrust so that the player is not slowed unintentionally with different gravity
			player.setDeltaMovement(motion.x * 0.7d, motion.y < 0 ? motion.y * 0.7d : motion.y, motion.z * 0.7d);
	}

	@Override
	protected boolean isValid(Block block) {
		return block instanceof SoulSandBlock || block == Blocks.SOUL_SOIL || registryNameContains(block, "soul_soil", "soul_sand");
	}

}

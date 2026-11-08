package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Every step makes a sound matching the block under the player */
public class SetEffectMusical extends SetEffect {

	protected SetEffectMusical() {
		super();
		this.color = LIGHT_PURPLE;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (isOnCooldown(player) || !(player.level() instanceof ServerLevel level))
			return;
		double dx = player.getX() - player.xo, dz = player.getZ() - player.zo;
		if (player.onGround() && Math.sqrt(dx * dx + dz * dz) > 0.1d) {
			setCooldown(player, 10);
			level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1f, player.getZ(), 1, 0.8f, 0.4f, 0.8f,
					level.getRandom().nextDouble());
			BlockState below = level.getBlockState(player.blockPosition().below());
			playSound(level, player.blockPosition(), below.instrument().getSoundEvent(), SoundSource.RECORDS, 3.0F,
					level.getRandom().nextFloat() * 2);
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "music", "note", "sound", "jukebox");
	}

}

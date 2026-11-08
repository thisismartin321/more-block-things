package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Makes it snow around the player, forms snow/frosted ice and drops snowballs (while the key is held) */
public class SetEffectSnowy extends SetEffect {

	protected SetEffectSnowy() {
		super();
		this.color = WHITE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || !(player.level() instanceof ServerLevel level))
			return;
		int radius = 3;
		var random = level.getRandom();
		level.sendParticles(ParticleTypes.WHITE_ASH, player.getX() + (random.nextDouble() - 0.5D) * radius,
				player.getY() + random.nextDouble() + 2D, player.getZ() + (random.nextDouble() - 0.5D) * radius, 5, 0, 0, 0, 0);
		for (int i = 0; i < 2; ++i)
			level.sendParticles(ParticleTypes.CLOUD, player.getX() + (random.nextDouble() - 0.5D) * radius,
					player.getY() + random.nextDouble() * 0.5d + 2.5D, player.getZ() + (random.nextDouble() - 0.5D) * radius, 5,
					0, 0, 0, 0);
		if (random.nextInt(2) == 0)
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WEATHER_RAIN,
					player.getSoundSource(), 0.1f, random.nextFloat());
		for (int x = -radius / 2; x <= radius / 2; x++)
			for (int z = -radius / 2; z <= radius / 2; z++)
				for (int y = 0; y <= 2; y++) {
					BlockPos pos = player.blockPosition().offset(x, y, z);
					BlockPos posBelow = player.blockPosition().offset(x, y - 1, z);
					if (player.mayBuild() && random.nextInt(100) == 0 && level.isEmptyBlock(pos)) {
						if (Blocks.SNOW.defaultBlockState().canSurvive(level, pos))
							level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState());
						else if (level.getBlockState(posBelow) == Blocks.WATER.defaultBlockState()
								|| level.getBlockState(posBelow).getBlock() == Blocks.FROSTED_ICE)
							level.setBlockAndUpdate(posBelow, Blocks.FROSTED_ICE.defaultBlockState());
					}
				}
		// snowballs
		if (random.nextInt(15) == 0) {
			ItemEntity item = new ItemEntity(level, player.getX() + (random.nextDouble() - 0.5D) * radius,
					player.getY() + random.nextDouble() + 1.5D, player.getZ() + (random.nextDouble() - 0.5D) * radius,
					new ItemStack(Items.SNOWBALL));
			item.setPickUpDelay(40);
			level.addFreshEntity(item);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOW_STEP, player.getSoundSource(),
					1.0f, random.nextFloat());
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "snow");
	}

}

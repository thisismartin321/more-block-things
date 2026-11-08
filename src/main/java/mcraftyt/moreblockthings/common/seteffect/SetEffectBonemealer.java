package mcraftyt.moreblockthings.common.seteffect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Fertilises plants nearby like bone meal */
public class SetEffectBonemealer extends SetEffect {

	protected SetEffectBonemealer() {
		super();
		this.color = WHITE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player) || !(player.level() instanceof ServerLevel level))
			return;
		int radius = 2;
		List<BlockPos> bonemealed = new ArrayList<>();
		for (int x = -radius; x < radius; x++)
			for (int y = -radius; y < radius; y++)
				for (int z = -radius; z < radius; z++) {
					BlockPos pos = player.blockPosition().offset(x, y, z);
					if (BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), level, pos, player))
						bonemealed.add(pos);
				}
		if (bonemealed.isEmpty()) {
			setCooldown(player, 20);
			return;
		}
		setCooldown(player, 100);
		for (BlockPos pos : bonemealed)
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX(), pos.getY() + 1d, pos.getZ(), 10, 2, 0.1d, 2, 0);
		playSound(level, player.blockPosition(), SoundEvents.HOE_TILL, SoundSource.PLAYERS, 0.5f,
				level.getRandom().nextFloat() + 0.5f);
		damageArmor(player, 4, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "bone", "fertil", "manure", "poop", "grow", "compost");
	}

}

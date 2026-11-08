package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;

/** Feeds the player automatically */
public class SetEffectFeeder extends SetEffect {

	protected SetEffectFeeder() {
		super();
		this.color = RED;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (isOnCooldown(player) || !player.canEat(false))
			return;
		int foodLevel = player.getFoodData().getFoodLevel();
		int foodToFeed = Math.min(20 - foodLevel, 4);
		player.getFoodData().eat(foodToFeed, 0.1f);
		float pitch = player.level().getRandom().nextFloat() * 0.2f + 1.0f;
		if (foodLevel < 16)
			for (int i = 0; i < 2; ++i)
				playSound(player.level(), player.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.15F, pitch);
		else
			playSound(player.level(), player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.15F, pitch);
		setCooldown(player, 80);
		damageArmor(player, foodToFeed, true);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "pumpkin", "melon", "food", "berry", "feed", "pork");
	}

}

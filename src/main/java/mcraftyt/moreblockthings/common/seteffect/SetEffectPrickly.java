package mcraftyt.moreblockthings.common.seteffect;

import java.util.List;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Thorns I on all pieces and damage to living entities that touch the player */
public class SetEffectPrickly extends SetEffect {

	protected SetEffectPrickly() {
		super();
		this.color = GREEN;
		for (net.minecraft.world.item.equipment.ArmorType type : ArmorSet.TYPES)
			addEnchantment(Enchantments.THORNS, 1, type);
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (isOnCooldown(player) || !(player.level() instanceof ServerLevel level))
			return;
		List<LivingEntity> list = new java.util.ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox()));
		list.remove(player);
		if (!list.isEmpty() && list.get(0).hurtServer(level, level.damageSources().cactus(), 1.0F)) {
			playSound(level, player.blockPosition(), SoundEvents.THORNS_HIT, SoundSource.PLAYERS, 1.0F,
					level.getRandom().nextFloat() * 0.4F + 0.8F);
			setCooldown(player, 20);
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "cactus", "sharp", "spike", "spine", "needle", "thorn");
	}

}

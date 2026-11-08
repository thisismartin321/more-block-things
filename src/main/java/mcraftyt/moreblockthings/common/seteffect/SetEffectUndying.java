package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Totem effect: instead of dying the player is revived (cooldown 5 minutes) */
public class SetEffectUndying extends SetEffect {

	protected SetEffectUndying() {
		super();
		this.color = DARK_PURPLE;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onDeath);
	}

	private void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide()
				|| !ArmorSet.hasSetEffect(player, this) || isOnCooldown(player))
			return;
		// Health to 1, clear effects, regeneration/absorption/fire resistance as with the totem
		player.setHealth(1);
		player.removeAllEffects();
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
		player.level().broadcastEntityEvent(player, (byte) 35); // Totem animation
		setCooldown(player, 6000);
		damageArmor(player, 100, true);
		playSound(player.level(), player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
		event.setCanceled(true);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "netherite", "ancient_debris");
	}

}

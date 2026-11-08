package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Instead of dying the player is teleported to their respawn point (cooldown 5 minutes) */
public class SetEffectRespawn extends SetEffect {

	protected SetEffectRespawn() {
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
		player.setHealth(1);
		player.removeAllEffects();
		player.clearFire();
		// Determine the respawn point (bed/anchor or world spawn, possibly another dimension) without consuming the bed/anchor
		TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
		ServerPlayer teleported = player.teleport(transition);
		ServerPlayer target = teleported != null ? teleported : player;
		setCooldown(target, 6000);
		damageArmor(target, 100, true);
		playSound(target.level(), target.blockPosition(), SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 1.0F, 1.0F);
		event.setCanceled(true);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "respawn");
	}

}

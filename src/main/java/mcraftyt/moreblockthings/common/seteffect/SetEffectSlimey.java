package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Bounces after falling as on a slime block, rebounds off walls, no fall damage (sneaking = off) */
public class SetEffectSlimey extends SetEffect {

	/** Only used on the client (movement is decided by the client); static is enough, two players in the same tick are unrealistic */
	private static LivingEntity bouncingEntity;
	private static double motionY;
	/** Client cooldown for the wall rebound (player ticks) */
	private static int wallBounceCooldownEnd;

	protected SetEffectSlimey() {
		super();
		this.color = GREEN;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onFall);
		bus.addListener(this::onPlayerTickPost);
	}

	@Override
	public void onClientTick(Player player) {
		if (player.isShiftKeyDown())
			return;
		// faster in the air
		if (!player.onGround() && !player.isFallFlying())
			player.setDeltaMovement(player.getDeltaMovement().x * 1.07d, player.getDeltaMovement().y,
					player.getDeltaMovement().z * 1.07d);
		// rebound off walls
		double dx = player.getX() - player.xo, dz = player.getZ() - player.zo;
		if (player.tickCount >= wallBounceCooldownEnd && player.horizontalCollision && Math.sqrt(dx * dx + dz * dz) >= 1.1D) {
			wallBounceCooldownEnd = player.tickCount + 10;
			double multiplier = 0.1d;
			if (player.getDeltaMovement().x == 0)
				player.setDeltaMovement(-dx * multiplier, player.getDeltaMovement().y + 0.1d, dz * multiplier);
			else if (player.getDeltaMovement().z == 0)
				player.setDeltaMovement(dx * multiplier, player.getDeltaMovement().y + 0.1d, -dz * multiplier);
			player.level().playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.SLIME_BLOCK_FALL,
					SoundSource.BLOCKS, 0.4F, 1.0F);
		}
	}

	private void onFall(LivingFallEvent event) {
		if (!(event.getEntity() instanceof Player player) || !ArmorSet.hasSetEffect(player, this))
			return;
		if (player.isShiftKeyDown()) {
			event.setDamageMultiplier(0.1f);
			return;
		}
		event.setDamageMultiplier(0);
		if (player.level().isClientSide()) {
			double distance = event.getDistance();
			double y = player.getDeltaMovement().y();
			if (distance <= 40 && distance > 2D)
				player.setDeltaMovement(player.getDeltaMovement().x, Math.abs(y * 0.9d), player.getDeltaMovement().z);
			else if (distance > 40 && distance <= 100)
				player.setDeltaMovement(player.getDeltaMovement().x, Math.abs(y * 0.9d * 1.5D), player.getDeltaMovement().z);
			else if (distance > 100)
				player.setDeltaMovement(player.getDeltaMovement().x, Math.abs(y * 0.9d * 2D), player.getDeltaMovement().z);
			if (distance > 2D)
				player.level().playSound(player, player.getX(), player.getY(), player.getZ(),
						distance > 40 ? SoundEvents.SLIME_JUMP : SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.4F, 1.0F);
			player.needsSync = true;
			player.setOnGround(false);
			player.hurtMarked = true;
			bouncingEntity = player;
			motionY = player.getDeltaMovement().y();
		} else {
			event.setCanceled(true);
		}
	}

	/** Sets the movement again after the fall tick so that the impact does not overwrite it */
	private void onPlayerTickPost(PlayerTickEvent.Post event) {
		if (bouncingEntity != null && event.getEntity() == bouncingEntity && bouncingEntity.level().isClientSide()) {
			bouncingEntity.setDeltaMovement(bouncingEntity.getDeltaMovement().x, motionY, bouncingEntity.getDeltaMovement().z);
			bouncingEntity.fallDistance = 0;
			bouncingEntity = null;
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "slime");
	}

}

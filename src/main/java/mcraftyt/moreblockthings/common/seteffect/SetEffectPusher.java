package mcraftyt.moreblockthings.common.seteffect;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/** Pushes entities within 5 blocks away from the player */
public class SetEffectPusher extends SetEffect {

	protected SetEffectPusher() {
		super();
		this.color = GRAY;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player))
			return;
		AABB aabb = player.getBoundingBox().inflate(5, 5, 5);
		List<Entity> list = player.level().getEntities(player, aabb);
		if (list.isEmpty()) {
			setCooldown(player, 5);
			return;
		}
		for (Entity entity : list)
			if (!(entity instanceof HangingEntity)) {
				double xVel = entity.getX() - player.getX();
				double yVel = entity.getY() - player.getY();
				double zVel = entity.getZ() - player.getZ();
				double velScale = 5 / Math.sqrt(xVel * xVel + yVel * yVel + zVel * zVel);
				entity.push(velScale * xVel, velScale * yVel, velScale * zVel);
				entity.hurtMarked = true;
			}
		playSound(player.level(), player.blockPosition(), SoundEvents.PISTON_EXTEND, SoundSource.PLAYERS, 0.5F,
				player.level().getRandom().nextFloat() + 0.5f);
		setCooldown(player, 40);
		damageArmor(player, 1, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "piston", "push", "repel") && !registryNameContains(block, "sticky");
	}

}

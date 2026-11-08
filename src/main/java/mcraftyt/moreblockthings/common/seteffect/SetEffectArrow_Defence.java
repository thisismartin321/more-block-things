package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Shoots 16 arrows in a circle around the player */
public class SetEffectArrow_Defence extends SetEffect {

	protected SetEffectArrow_Defence() {
		super();
		this.color = WHITE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player))
			return;
		int numArrows = 16;
		for (int i = 0; i < numArrows; i++) {
			Arrow arrow = new Arrow(player.level(), player, new ItemStack(Items.ARROW), null);
			arrow.shootFromRotation(player, 0.0F, player.getYRot() + i * (360 / numArrows), 0.0F, 2.0F, 0.0F);
			arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
			player.level().addFreshEntity(arrow);
		}
		playSound(player.level(), player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
				player.level().getRandom().nextFloat() + 0.5f);
		setCooldown(player, 40);
		damageArmor(player, 4, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "dispense", "shoot", "arrow", "fletch");
	}

}

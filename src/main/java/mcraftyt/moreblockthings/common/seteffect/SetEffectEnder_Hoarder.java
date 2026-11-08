package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnderChestBlock;

/** Opens the ender chest with the key */
public class SetEffectEnder_Hoarder extends SetEffect {

	protected SetEffectEnder_Hoarder() {
		super();
		this.color = DARK_PURPLE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player) || player.containerMenu instanceof ChestMenu)
			return;
		setCooldown(player, 10);
		playSound(player.level(), player.blockPosition(), SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.5F,
				player.level().getRandom().nextFloat() * 0.1F + 0.9F);
		player.openMenu(new SimpleMenuProvider(
				(id, inventory, p) -> ChestMenu.threeRows(id, inventory, player.getEnderChestInventory()),
				Component.translatable("container.enderchest")));
		damageArmor(player, 1, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return block instanceof EnderChestBlock;
	}

}

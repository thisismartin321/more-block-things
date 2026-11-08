package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.block.Block;

/** Opens a crafting table with the key (anywhere, without a block nearby) */
public class SetEffectCrafter extends SetEffect {

	protected SetEffectCrafter() {
		super();
		this.color = GOLD;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player))
			return;
		setCooldown(player, 10); // prevents opening multiple times while the key is held
		player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new CraftingMenu(id, inventory,
				ContainerLevelAccess.create(p.level(), p.blockPosition())) {
			/** Stays open even if no crafting table is nearby */
			@Override
			public boolean stillValid(Player p) {
				return true;
			}
		}, Component.translatable("container.crafting")));
		damageArmor(player, 1, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "crafting", "assemble");
	}

}

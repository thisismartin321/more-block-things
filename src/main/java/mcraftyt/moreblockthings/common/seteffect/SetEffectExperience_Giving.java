package mcraftyt.moreblockthings.common.seteffect;

import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;

/** Gives experience regularly (or repairs items with Mending) */
public class SetEffectExperience_Giving extends SetEffect {

	protected SetEffectExperience_Giving() {
		super();
		this.color = GREEN;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (isOnCooldown(player))
			return;
		setCooldown(player, 50);
		// Give experience so that Mending works; modelled on ExperienceOrb
		boolean hasMending = false;
		Optional<EnchantedItemInUse> entry = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP,
				player, ItemStack::isDamaged);
		if (entry.isPresent()) {
			ItemStack stack = entry.get().itemStack();
			if (!stack.isEmpty() && stack.isDamaged()) {
				stack.setDamageValue(stack.getDamageValue() - 1);
				hasMending = true;
			}
		}
		if (!hasMending)
			player.giveExperiencePoints(1);
		damageArmor(player, 1, false);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "lapis", "enchant", "experience", "amethyst", "ruby", "peridot", "topaz",
				"tanzanite", "malachite", "sapphire", "amber");
	}

}

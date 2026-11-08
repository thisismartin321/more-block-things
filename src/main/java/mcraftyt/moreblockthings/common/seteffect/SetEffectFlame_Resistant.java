package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public class SetEffectFlame_Resistant extends SetEffect {

	protected SetEffectFlame_Resistant() {
		super();
		this.color = DARK_RED;
		for (ArmorType type : mcraftyt.moreblockthings.common.item.ArmorSet.TYPES)
			addEnchantment(Enchantments.FIRE_PROTECTION, 4, type);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "obsidian", "magma", "fire", "flame", "lava") && !registryNameContains(block, "coral");
	}

}

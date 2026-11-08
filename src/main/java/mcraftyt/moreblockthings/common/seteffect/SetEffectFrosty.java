package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public class SetEffectFrosty extends SetEffect {

	protected SetEffectFrosty() {
		super();
		this.color = AQUA;
		addEnchantment(Enchantments.FROST_WALKER, 2, ArmorType.BOOTS);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "ice", "snow", "frozen", "frost");
	}

}

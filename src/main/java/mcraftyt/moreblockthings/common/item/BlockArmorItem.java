package mcraftyt.moreblockthings.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * A piece of armor of an {@link ArmorSet}. In modern Minecraft almost everything lives in data components
 * (Item.Properties#humanoidArmor); the class only holds the link to the set.
 * Set effects are handled in the seteffect package.
 */
public class BlockArmorItem extends Item {

	public final ArmorSet set;
	public final ArmorType type;

	public BlockArmorItem(Item.Properties properties, ArmorSet set, ArmorType type) {
		super(properties);
		this.set = set;
		this.type = type;
	}

}

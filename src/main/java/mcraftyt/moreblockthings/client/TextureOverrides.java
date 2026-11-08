package mcraftyt.moreblockthings.client;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/**
 * Special blocks whose armor/icon does not come from the block sides but from their own textures
 * (assets/moreblockthings/textures/item/override/&lt;name&gt;.png) - like TEXTURE_OVERRIDES in the original.
 * Coloured shulker boxes and beds use the white texture, tinted with the block's colour.
 */
public class TextureOverrides {

	/** Name of the texture (without path) and fixed tint (0 = none) */
	public record Info(String texture, int tint) {
	}

	private static String slot(ArmorType type) {
		return switch (type) {
			case HELMET -> "head";
			case CHESTPLATE -> "chest";
			case LEGGINGS -> "legs";
			case BOOTS -> "feet";
			default -> "body";
		};
	}

	private static int tint(DyeColor color) {
		return color == null ? 0 : (color.getFireworkColor() | 0xFF000000);
	}

	/** Override for this block and this body part, or null */
	public static Info get(Block block, ArmorType type) {
		String slot = slot(type);
		if (block == Blocks.ENCHANTING_TABLE)
			return type == ArmorType.CHESTPLATE || type == ArmorType.LEGGINGS ? new Info("enchanting_table_" + slot, 0) : null;
		if (block == Blocks.COMPOSTER)
			return type == ArmorType.HELMET ? new Info("composter_head", 0) : null;
		if (block == Blocks.CACTUS)
			return new Info("cactus_" + slot, 0);
		if (block == Blocks.SUGAR_CANE)
			return new Info("sugar_cane_" + slot, 0);
		if (block == Blocks.ENDER_CHEST)
			return new Info("ender_chest_" + slot, 0);
		if (block == Blocks.CHEST)
			return new Info("chest_" + slot, 0);
		if (block instanceof ShulkerBoxBlock shulker)
			return shulker.getColor() == null ? new Info("shulker_box_" + slot, 0)
					: new Info("white_shulker_box_" + slot, tint(shulker.getColor()));
		// In the original, coloured beds stayed untinted; tinted they look like real beds
		if (block instanceof BedBlock bed)
			return new Info("white_bed_" + slot, tint(bed.getColor()));
		return null;
	}

}

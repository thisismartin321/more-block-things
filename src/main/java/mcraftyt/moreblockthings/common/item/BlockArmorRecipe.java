package mcraftyt.moreblockthings.common.item;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

/**
 * One special recipe that covers every armor set: the pieces are crafted from the set's block in the same shapes as
 * vanilla armor (helmet 5, chestplate 8, leggings 7, boots 4 blocks). A single recipe is used instead of thousands of
 * generated JSON files because the sets are created dynamically from all registered blocks.
 */
public class BlockArmorRecipe extends CustomRecipe {

	public static final BlockArmorRecipe INSTANCE = new BlockArmorRecipe();

	public static final RecipeSerializer<BlockArmorRecipe> SERIALIZER = new RecipeSerializer<>(MapCodec.unit(INSTANCE),
			StreamCodec.unit(INSTANCE));

	/** Shapes as {width, height, row-major cells}; 1 = block, 0 = empty. Same order as {@link ArmorSet#TYPES}. */
	private static final int[][] SHAPES = {
			// helmet
			{3, 2, 1, 1, 1, 1, 0, 1},
			// chestplate
			{3, 3, 1, 0, 1, 1, 1, 1, 1, 1, 1},
			// leggings
			{3, 3, 1, 1, 1, 1, 0, 1, 1, 0, 1},
			// boots
			{3, 2, 1, 0, 1, 1, 0, 1}};

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return find(input) != null;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack result = find(input);
		return result == null ? ItemStack.EMPTY : result;
	}

	@Override
	public RecipeSerializer<BlockArmorRecipe> getSerializer() {
		return SERIALIZER;
	}

	/** The armor piece produced by this crafting grid, or null if the grid does not match any shape */
	private static ItemStack find(CraftingInput input) {
		for (int t = 0; t < SHAPES.length; t++) {
			int[] shape = SHAPES[t];
			// CraftingInput has already been trimmed of empty rows and columns
			if (input.width() != shape[0] || input.height() != shape[1])
				continue;

			ArmorSet set = null;
			boolean valid = true;
			for (int i = 0; i < input.size() && valid; i++) {
				ItemStack stack = input.getItem(i);
				boolean required = shape[2 + i] == 1;
				if (stack.isEmpty() != !required) {
					valid = false;
				} else if (required) {
					if (stack.getItem() instanceof BlockArmorItem) {
						valid = false;
						break;
					}
					ArmorSet candidate = ArmorSet.getSet(stack.getItem());
					if (candidate == null || !candidate.enabled || (set != null && set != candidate))
						valid = false;
					else
						set = candidate;
				}
			}
			if (valid && set != null) {
				ArmorType type = ArmorSet.TYPES[t];
				BlockArmorItem piece = set.getPiece(type);
				if (piece != null)
					return new ItemStack(piece);
			}
		}
		return null;
	}

}

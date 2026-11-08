package mcraftyt.moreblockthings.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import mcraftyt.moreblockthings.common.MoreBlockThings;

/** Registers the special crafting recipe (data/moreblockthings/recipe/block_armor.json refers to it) */
public class ModRecipes {

	public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
			.create(Registries.RECIPE_SERIALIZER, MoreBlockThings.MODID);

	static {
		SERIALIZERS.register("block_armor", () -> BlockArmorRecipe.SERIALIZER);
	}

}

package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.NetherrackBlock;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/** Slowly repairs the armor's durability */
public class SetEffectRegrowth extends SetEffect {

	protected SetEffectRegrowth() {
		super();
		this.color = DARK_GREEN;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (player.level().getRandom().nextInt(200) != 0)
			return;
		for (net.minecraft.world.item.equipment.ArmorType type : ArmorSet.TYPES) {
			ItemStack stack = player.getItemBySlot(type.getSlot());
			if (stack.getItem() instanceof BlockArmorItem item && item.set.setEffects.contains(this) && stack.isDamaged()) {
				stack.setDamageValue(stack.getDamageValue() - 1);
				return; // as in the original: one piece per activation
			}
		}
	}

	@Override
	protected boolean isValid(Block block) {
		if (block instanceof NetherrackBlock) // netherrack is bonemealable but should not count
			return false;
		// Replacement for the old material checks (plants, leaves, cactus, bamboo, ...)
		return block instanceof BonemealableBlock || block instanceof BushBlock || block instanceof LeavesBlock
				|| block instanceof CactusBlock || registryNameContains(block, "moss", "plant", "mycelium", "mushroom",
						"flower", "log", "wood", "stem", "plank", "grass", "nether_wart", "bamboo", "vine", "kelp", "leaves");
	}

}

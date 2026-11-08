package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;

public class SetEffectDiving_Suit extends SetEffect {

	protected SetEffectDiving_Suit() {
		super();
		this.color = DARK_AQUA;
		this.potionEffects.add(new MobEffectInstance(MobEffects.NIGHT_VISION, 210, 0, true, false));
		addEnchantment(Enchantments.AQUA_AFFINITY, 3, ArmorType.HELMET);
		addEnchantment(Enchantments.RESPIRATION, 3, ArmorType.HELMET);
		addEnchantment(Enchantments.DEPTH_STRIDER, 3, ArmorType.BOOTS);
	}

	/** Night vision only while the head is underwater */
	@Override
	protected boolean shouldApplyEffect(MobEffectInstance effect, Player player) {
		return !player.level().getBlockState(BlockPos.containing(player.getX(), player.getY() + 1.7d, player.getZ()))
				.getFluidState().isEmpty();
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "water", "prismarine", "sea", "coral", "kelp");
	}

}

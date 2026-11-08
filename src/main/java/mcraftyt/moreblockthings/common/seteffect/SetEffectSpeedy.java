package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SetEffectSpeedy extends SetEffect {

	protected SetEffectSpeedy() {
		super();
		this.color = YELLOW;
		addAttribute(Attributes.MOVEMENT_SPEED, 0.1d, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "fast", "speed", "sugar") || block == Blocks.SUGAR_CANE || block.getSpeedFactor() > 1f;
	}

}

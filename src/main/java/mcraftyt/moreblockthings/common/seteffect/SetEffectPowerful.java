package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;

public class SetEffectPowerful extends SetEffect {

	protected SetEffectPowerful() {
		super();
		this.color = WHITE;
		addAttribute(Attributes.ATTACK_SPEED, 1d, AttributeModifier.Operation.ADD_VALUE);
		addAttribute(Attributes.ATTACK_DAMAGE, 3d, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "quartz", "strong", "power");
	}

}

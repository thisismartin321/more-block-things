package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import mcraftyt.moreblockthings.common.utils.BlockUtils;

/** Knockback resistance, strength depends on the block hardness */
public class SetEffectImmovable extends SetEffect {

	private final double knockbackResistance;

	protected SetEffectImmovable(double knockbackResistance) {
		super();
		this.knockbackResistance = knockbackResistance;
		init();
	}

	private SetEffectImmovable(double knockbackResistance, boolean register) {
		super(register);
		this.knockbackResistance = knockbackResistance;
		init();
	}

	private void init() {
		this.color = GRAY;
		addAttribute(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	public Object[] getDescriptionObjects() {
		return new Object[] { (int) (knockbackResistance * 100d) };
	}

	@Override
	public String writeToString() {
		return name + " (" + knockbackResistance + ")";
	}

	@Override
	protected SetEffect fromString(String argument) {
		return argument == null ? this : new SetEffectImmovable(Double.parseDouble(argument), false);
	}

	@Override
	protected SetEffect create(Block block) {
		double hardness = BlockUtils.getHardness(block);
		if (hardness == -1) // indestructible
			hardness = 100;
		else
			hardness = Math.max(25, Math.min(100, hardness * 1.5d)); // between 25 and 100
		return new SetEffectImmovable(hardness / 100d, false);
	}

	@Override
	protected boolean isValid(Block block) {
		if (registryNameContains(block, "bedrock", "obsidian", "brick", "heavy", "sturdy")) {
			float hardness = BlockUtils.getHardness(block);
			return hardness == -1 || hardness >= 2;
		}
		return false;
	}

}

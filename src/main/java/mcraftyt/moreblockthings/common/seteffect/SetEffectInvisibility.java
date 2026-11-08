package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;

public class SetEffectInvisibility extends SetEffect {

	protected SetEffectInvisibility() {
		super();
		this.color = WHITE;
		this.potionEffects.add(new MobEffectInstance(MobEffects.INVISIBILITY, 10, 0, true, false));
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "glass", "ghost", "invisible");
	}

}

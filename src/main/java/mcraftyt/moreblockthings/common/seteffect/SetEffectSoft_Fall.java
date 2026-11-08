package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Immunity to fall damage */
public class SetEffectSoft_Fall extends SetEffect {

	protected SetEffectSoft_Fall() {
		super();
		this.color = WHITE;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onFall);
	}

	private void onFall(LivingFallEvent event) {
		if (ArmorSet.getWornSetEffects(event.getEntity()).contains(this)) {
			if (!event.getEntity().level().isClientSide() && event.getDistance() > 2)
				playSound(event.getEntity().level(), event.getEntity().blockPosition(), SoundEvents.WOOL_FALL,
						SoundSource.PLAYERS, (float) Math.min(event.getDistance() / 20f, 1),
						event.getEntity().level().getRandom().nextFloat() + 0.8f);
			event.setDistance(0);
			event.setDamageMultiplier(0);
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "wool", "hay", "soft");
	}

}

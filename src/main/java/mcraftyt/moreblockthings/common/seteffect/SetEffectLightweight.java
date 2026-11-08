package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Barely any fall damage and slow gliding (leaves/feathers) */
public class SetEffectLightweight extends SetEffect {

	protected SetEffectLightweight() {
		super();
		this.color = GREEN;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onFall);
	}

	private void onFall(LivingFallEvent event) {
		if (ArmorSet.hasSetEffect(event.getEntity(), this)) {
			if (event.getEntity().isShiftKeyDown()) {
				event.setDamageMultiplier(0.1f);
			} else {
				event.setDamageMultiplier(0.01f);
				event.setDistance(event.getDistance() / 10f);
			}
		}
	}

	/** Gliding (client-side, because movement is decided there) */
	@Override
	public void onClientTick(Player player) {
		if (!player.isShiftKeyDown() && player.getDeltaMovement().y() < 0 && !player.onGround()
				&& player.level().isEmptyBlock(player.blockPosition().below(2))) {
			if (player.fallDistance > 1 && player.getDeltaMovement().y() < -0.3d) {
				player.push(0, 0.076D, 0);
				if (player.fallDistance > 5) {
					double driftX = player.level().getRandom().nextDouble() - 0.5d;
					double driftZ = player.level().getRandom().nextDouble() - 0.5d;
					player.push(driftX / 5d, 0, driftZ / 5d);
					if (Math.abs(driftX) + Math.abs(driftZ) >= 0.75d)
						playSound(player.level(), player.blockPosition(), SoundEvents.GRASS_STEP, SoundSource.PLAYERS, 0.08F,
								player.level().getRandom().nextFloat() / 5);
				}
			}
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "leaves", "feather") || block instanceof LeavesBlock;
	}

}

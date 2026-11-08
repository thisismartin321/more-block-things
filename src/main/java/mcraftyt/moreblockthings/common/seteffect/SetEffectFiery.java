package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Sets attackers and attacked targets on fire */
public class SetEffectFiery extends SetEffect {

	protected SetEffectFiery() {
		super();
		this.color = RED;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onDamage);
	}

	private void onDamage(LivingIncomingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide())
			return;
		LivingEntity attacked = event.getEntity();
		// Sets whoever attacks the wearer on fire
		if (ArmorSet.hasSetEffect(attacked, this) && !attacker.isInWater()) {
			if (!attacker.isOnFire() && !attacker.fireImmune())
				playSound(attacker.level(), attacker.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.2f, 1.0f);
			attacker.igniteForSeconds(5);
		}
		// Sets the wearer's target on fire
		if (ArmorSet.hasSetEffect(attacker, this) && !attacked.isInWater()) {
			if (!attacked.isOnFire() && !attacked.fireImmune())
				playSound(attacked.level(), attacked.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.4f, 1.0f);
			attacked.igniteForSeconds(5);
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "netherrack", "magma", "fire", "flame", "lava", "nylium")
				&& !registryNameContains(block, "coral");
	}

}

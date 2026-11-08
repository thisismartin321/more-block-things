package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.Vec3;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Faster falling while sneaking, dust particles for falling blocks */
public class SetEffectFalling extends SetEffect {

	protected SetEffectFalling() {
		super();
		this.color = GRAY;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (player.level() instanceof ServerLevel level && level.getRandom().nextInt(16) == 0)
			for (ArmorSet set : wornSets(player))
				if (set.block instanceof FallingBlock) {
					level.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, set.block.defaultBlockState()),
							player.getX(), player.getY() + 1d, player.getZ(), 1, 0.3f, 0.5f, 0.3f, 0);
					break;
				}
	}

	@Override
	public void onClientTick(Player player) {
		Vec3 motion = player.getDeltaMovement();
		if (player.isShiftKeyDown() && Math.abs(motion.y) < 3.5d && motion.y < 0)
			player.setDeltaMovement(motion.x, motion.y * 1.3d, motion.z);
	}

	@Override
	protected boolean isValid(Block block) {
		return block instanceof FallingBlock || DIVING_SUIT.isValid(block) || registryNameContains(block, "dripstone");
	}

	/** Sets of which the player currently wears this effect */
	private java.util.List<ArmorSet> wornSets(Player player) {
		java.util.List<ArmorSet> sets = new java.util.ArrayList<>();
		for (net.minecraft.world.item.equipment.ArmorType type : ArmorSet.TYPES)
			if (player.getItemBySlot(type.getSlot()).getItem() instanceof mcraftyt.moreblockthings.common.item.BlockArmorItem item
					&& item.set.setEffects.contains(this) && !sets.contains(item.set))
				sets.add(item.set);
		return sets;
	}

}

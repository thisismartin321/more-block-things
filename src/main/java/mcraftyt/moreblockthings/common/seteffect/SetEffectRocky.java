package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** Sinks in water like a stone and walks along the bottom (via access transformer on Entity#wasTouchingWater/firstTick) */
public class SetEffectRocky extends SetEffect {

	/** Factor per tick for the speed on the bottom (0.14 was practically immobile, 1 = no braking) */
	private static final double GROUND_SLOWDOWN = 0.45d;

	protected SetEffectRocky() {
		super();
		this.color = GRAY;
	}

	/**
	 * Must run AFTER the water calculation (Entity#baseTick) and BEFORE movement (LivingEntity#travel), otherwise
	 * wasTouchingWater is overwritten again immediately. That is why it is called from the client event MovementInputUpdateEvent
	 * (see ClientEffectHooks). The local player's movement is decided by the client, so this is enough.
	 */
	public void applyBeforeTravel(Player player) {
		if (!player.isInWater())
			return;
		Vec3 motion = player.getDeltaMovement();
		player.setSwimming(false); // no swimming
		// set "not in water" so the player sinks and can walk normally
		player.wasTouchingWater = false;
		// firstTick = true so no bubble particles and sounds are produced
		player.firstTick = true;
		// slightly slower underwater (without Depth Strider)
		if ((Math.abs(motion.x) > 0 || Math.abs(motion.z) > 0 || motion.y > 0)
				&& EnchantmentHelper.getEnchantmentLevel(enchantment(player.level(), Enchantments.DEPTH_STRIDER), player) == 0) {
			player.setSprinting(false); // no sprinting
			player.setDeltaMovement(motion.x() * (player.onGround() ? GROUND_SLOWDOWN : 1d), motion.y(),
					motion.z() * (player.onGround() ? GROUND_SLOWDOWN : 1d));
			if (player.onGround())
				player.hurtMarked = true;
			player.needsSync = true;
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "rock", "stone", "deepslate", "tuff");
	}

}

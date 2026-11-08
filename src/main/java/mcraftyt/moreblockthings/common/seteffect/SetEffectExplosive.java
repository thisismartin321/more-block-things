package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** TNT armor: pressing the key creates an explosion around the player */
public class SetEffectExplosive extends SetEffect {

	protected SetEffectExplosive() {
		super();
		this.color = RED;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (SetEffectEvents.isKeyDown(player) && !isOnCooldown(player))
			tryExplode(this, player);
	}

	/** Explosion around the player (strength 6, blocks are destroyed, as in the original) */
	protected static void tryExplode(SetEffect effect, ServerPlayer player) {
		if (!player.mayBuild())
			return;
		effect.setCooldown(player, 20);
		if (player.level() instanceof ServerLevel level)
			level.explode(player, null, null, new Vec3(player.getX(), player.getY() + 0.5d, player.getZ()), 6f, false,
					Level.ExplosionInteraction.BLOCK);
		effect.damageArmor(player, 10, true);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "tnt", "explo");
	}

}

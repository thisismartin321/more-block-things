package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.util.ClockAdjustment;
import net.neoforged.neoforge.event.EventHooks;

/** Skips the night (like sleeping). In dimensions where beds explode it explodes instead. */
public class SetEffectSleepy extends SetEffect {

	protected SetEffectSleepy() {
		super();
		this.color = WHITE;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player) || !(player.level() instanceof ServerLevel level))
			return;
		BedRule rule = level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, player.blockPosition());
		if (rule.explodes()) {
			// Dimension where beds explode: like Explosive
			SetEffectExplosive.tryExplode(this, player);
		} else if (rule.canSleep(level)) {
			// Skip the night: copied from ServerLevel#tick (sleeping)
			if (level.getGameRules().get(GameRules.ADVANCE_TIME))
				level.dimensionType().defaultClock().ifPresent(clock -> {
					ClockAdjustment adjustment = EventHooks.onSleepFinished(level,
							new ClockAdjustment.Marker(ClockTimeMarkers.WAKE_UP_FROM_SLEEP));
					if (adjustment != null)
						adjustment.apply(level.clockManager(), clock);
				});
			level.wakeUpAllPlayers(); // made public via access transformer
			if (level.getGameRules().get(GameRules.ADVANCE_WEATHER) && level.isRaining())
				level.resetWeatherCycle();
			playSound(level, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 1.4f);
			setCooldown(player, 100);
		} else {
			// sleeping not possible (daytime)
			playSound(level, player.blockPosition(), SoundEvents.NOTE_BLOCK_BASS, SoundSource.PLAYERS, 1.0F,
					level.getRandom().nextFloat() + 0.5F);
			setCooldown(player, 10);
		}
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "bed", "sleep", "hammock") && !registryNameContains(block, "bedrock");
	}

}

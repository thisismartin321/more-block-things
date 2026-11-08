package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;

/**
 * Rewind, stop or accelerate time while the key is held. Depending on the command block type:
 * Repeating = Rewind, Chain = Stop, Normal = Accelerate. Runs via the new ServerClockManager.
 */
public class SetEffectTime_Control extends SetEffect {

	private enum Type {
		REWIND(Blocks.REPEATING_COMMAND_BLOCK), STOP(Blocks.CHAIN_COMMAND_BLOCK), ACCELERATE(Blocks.COMMAND_BLOCK);

		final Block block;

		Type(Block block) {
			this.block = block;
		}

		static Type of(Block block) {
			for (Type type : values())
				if (type.block == block)
					return type;
			return ACCELERATE;
		}
	}

	private final Type type;

	/** Template (registered) */
	protected SetEffectTime_Control() {
		super();
		this.type = Type.ACCELERATE;
		this.color = LIGHT_PURPLE;
		this.usesButton = true;
	}

	/** Instance per set */
	private SetEffectTime_Control(Type type) {
		super(false);
		this.type = type;
		this.color = LIGHT_PURPLE;
		this.usesButton = true;
	}

	@Override
	public Component getDescription() {
		return Component.translatable("setEffect." + key() + "." + type.name().toLowerCase() + ".description");
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || !(player.level() instanceof ServerLevel level))
			return;
		level.dimensionType().defaultClock().ifPresent(clock -> {
			var manager = level.clockManager();
			switch (type) {
			case REWIND -> {
				manager.addTicks(clock, -21);
				if (player.tickCount % 4 == 0)
					playSound(level, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.3f, 0f);
			}
			case STOP -> {
				// Time advances by 1 per tick; we subtract that again (only if it is running at all)
				if (level.getGameRules().get(GameRules.ADVANCE_TIME)) {
					manager.addTicks(clock, -1);
					if (player.tickCount % 8 == 0)
						playSound(level, player.blockPosition(), SoundEvents.NOTE_BLOCK_SNARE, SoundSource.PLAYERS, 0.2f, 0f);
				}
			}
			case ACCELERATE -> {
				manager.addTicks(clock, 19);
				if (player.tickCount % 2 == 0)
					playSound(level, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.3f, 2f);
			}
			}
		});
	}

	@Override
	public String writeToString() {
		return name + " (" + type.name() + ")";
	}

	@Override
	protected SetEffect fromString(String argument) {
		return argument == null ? this : new SetEffectTime_Control(Type.valueOf(argument.trim().toUpperCase()));
	}

	@Override
	protected SetEffect create(Block block) {
		return new SetEffectTime_Control(Type.of(block));
	}

	@Override
	protected boolean isValid(Block block) {
		return block == Blocks.REPEATING_COMMAND_BLOCK || block == Blocks.CHAIN_COMMAND_BLOCK || block == Blocks.COMMAND_BLOCK;
	}

}

package mcraftyt.moreblockthings.common.seteffect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import mcraftyt.moreblockthings.common.utils.BlockUtils;

/**
 * Luminous blocks light up their surroundings. Instead of a dedicated "moving light block" as in the original, the
 * effect places a vanilla light block at head height and removes it again when moving on. The key toggles it on/off.
 */
public class SetEffectIlluminated extends SetEffect {

	/** Position of the player's current light block */
	private static final Map<UUID, BlockPos> LIGHTS = new HashMap<>();
	/** Players who have switched the light off */
	private static final Set<UUID> DISABLED = new HashSet<>();

	private final int lightLevel;

	/** Template (registered) */
	protected SetEffectIlluminated(int lightLevel) {
		super();
		this.lightLevel = Mth.clamp(lightLevel, 1, 15);
		init();
	}

	/** Instance per set */
	private SetEffectIlluminated(int lightLevel, boolean register) {
		super(register);
		this.lightLevel = Mth.clamp(lightLevel, 1, 15);
		init();
	}

	private void init() {
		this.color = GOLD;
		this.usesButton = true;
	}

	@Override
	public Object[] getDescriptionObjects() {
		return new Object[] { lightLevel };
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
			if (event.getEntity().level() instanceof ServerLevel level)
				removeLight(level, event.getEntity().getUUID());
			DISABLED.remove(event.getEntity().getUUID());
		});
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!(player.level() instanceof ServerLevel level))
			return;
		UUID id = player.getUUID();
		// toggle on/off
		if (SetEffectEvents.isKeyDown(player) && !isOnCooldown(player)) {
			boolean nowDisabled = DISABLED.add(id);
			if (!nowDisabled)
				DISABLED.remove(id);
			player.sendSystemMessage(Component.literal("Illuminated set effect " + (nowDisabled ? "disabled" : "enabled"))
					.withStyle(style -> style.withColor(nowDisabled ? RED : GREEN).withItalic(true)));
			setCooldown(player, 10);
		}
		if (DISABLED.contains(id)) {
			removeLight(level, id);
			return;
		}
		BlockPos target = player.blockPosition().above();
		BlockPos old = LIGHTS.get(id);
		if (!target.equals(old)) {
			removeLight(level, id);
			if (level.isEmptyBlock(target) && level.getBrightness(LightLayer.BLOCK, target) < lightLevel) {
				level.setBlockAndUpdate(target, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel));
				LIGHTS.put(id, target);
			}
		}
	}

	@Override
	public void onInactive(ServerPlayer player) {
		super.onInactive(player);
		if (LIGHTS.containsKey(player.getUUID()) && player.level() instanceof ServerLevel level)
			removeLight(level, player.getUUID());
	}

	/** Removes the player's light block (only if our light block is still there) */
	private static void removeLight(Level level, UUID id) {
		BlockPos pos = LIGHTS.remove(id);
		if (pos != null && level.getBlockState(pos).is(Blocks.LIGHT))
			level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
	}

	@Override
	public String writeToString() {
		return name + " (" + lightLevel + ")";
	}

	@Override
	protected SetEffect fromString(String argument) {
		return argument == null ? this : new SetEffectIlluminated(Integer.parseInt(argument.trim()), false);
	}

	@Override
	protected SetEffect create(Block block) {
		return new SetEffectIlluminated(BlockUtils.getLightLevel(block), false);
	}

	@Override
	protected boolean isValid(Block block) {
		return BlockUtils.getLightLevel(block) > 0;
	}

}

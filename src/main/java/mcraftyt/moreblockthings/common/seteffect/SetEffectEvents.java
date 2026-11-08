package mcraftyt.moreblockthings.common.seteffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/** Game-bus events: calls active set effects every tick and remembers the key state per player */
public class SetEffectEvents {

	/** Key state per player, arrives from the client via packet */
	private static final Map<UUID, Boolean> KEY_DOWN = new HashMap<>();

	public static boolean isKeyDown(Player player) {
		return player != null && KEY_DOWN.getOrDefault(player.getUUID(), false);
	}

	public static void setKeyDown(UUID player, boolean down) {
		KEY_DOWN.put(player, down);
	}

	/** Registers the effects' own listeners and the shared ones */
	public static void register(IEventBus bus) {
		bus.addListener(SetEffectEvents::onPlayerTick);
		bus.addListener(SetEffectEvents::onLogout);
		bus.addListener(SetEffectEvents::onEnchantmentLevel);
		for (SetEffect effect : SetEffect.SET_EFFECTS)
			effect.registerEvents(bus);
	}

	/** Which effects were active during the last tick (instances can differ per set) */
	private static final Map<UUID, Set<SetEffect>> LAST_ACTIVE = new HashMap<>();

	private static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		Set<SetEffect> active = ArmorSet.getWornSetEffects(player);
		if (player instanceof ServerPlayer serverPlayer) {
			Set<SetEffect> last = LAST_ACTIVE.getOrDefault(serverPlayer.getUUID(), Set.of());
			for (SetEffect effect : last)
				if (!active.contains(effect))
					effect.onInactive(serverPlayer);
			for (SetEffect effect : active)
				effect.onTick(serverPlayer);
			LAST_ACTIVE.put(serverPlayer.getUUID(), new java.util.HashSet<>(active));
		} else if (player.level().isClientSide() && player.isLocalPlayer()) {
			for (SetEffect effect : active)
				effect.onClientTick(player);
		}
	}

	private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		UUID id = event.getEntity().getUUID();
		KEY_DOWN.remove(id);
		LAST_ACTIVE.remove(id);
		for (SetEffect effect : SetEffect.SET_EFFECTS)
			effect.clearState(id);
	}

	/**
	 * Enchantments of the effects (e.g. Fire Protection IV): instead of changing the items, the level is
	 * raised when queried. Applies to every armor piece whose set has the effect (even if the whole set is not worn).
	 */
	private static void onEnchantmentLevel(GetEnchantmentLevelEvent event) {
		if (!(event.getStack() instanceof ItemStack stack) || !(stack.getItem() instanceof BlockArmorItem item))
			return;
		for (SetEffect effect : item.set.setEffects)
			for (SetEffect.EnchantmentDef def : effect.enchantments)
				if (def.slot() == item.type && event.isTargetting(def.key()))
					event.getHolder(def.key()).ifPresent(holder -> {
						if (event.getEnchantments().getLevel(holder) < def.level())
							event.getEnchantments().set(holder, def.level());
					});
	}

}

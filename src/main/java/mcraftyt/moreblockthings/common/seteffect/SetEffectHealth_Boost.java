package mcraftyt.moreblockthings.common.seteffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.utils.BlockUtils;

/** More maximum health; the amount depends on the block hardness */
public class SetEffectHealth_Boost extends SetEffect {

	/** Players whose health should be refilled after login/respawn */
	private static final Set<UUID> PLAYERS_TO_CHECK = new HashSet<>();

	private final double healthBoost;

	/** Template (registered, default value) */
	protected SetEffectHealth_Boost(double healthBoost) {
		super();
		this.healthBoost = healthBoost;
		init();
	}

	/** Instance per set */
	private SetEffectHealth_Boost(double healthBoost, boolean register) {
		super(register);
		this.healthBoost = healthBoost;
		init();
	}

	private void init() {
		this.color = RED;
		addAttribute(Attributes.MAX_HEALTH, healthBoost, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	public Object[] getDescriptionObjects() {
		return new Object[] { (int) healthBoost / 2 };
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener((PlayerEvent.PlayerLoggedInEvent e) -> PLAYERS_TO_CHECK.add(e.getEntity().getUUID()));
		bus.addListener((PlayerEvent.PlayerRespawnEvent e) -> PLAYERS_TO_CHECK.add(e.getEntity().getUUID()));
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		// After login/respawn with the set: refill the health of this effect if the player would otherwise stand at 20
		if (PLAYERS_TO_CHECK.remove(player.getUUID()) && player.getHealth() >= 20f) {
			float amount = 0;
			for (SetEffect effect : ArmorSet.getWornSetEffects(player))
				if (effect instanceof SetEffectHealth_Boost boost)
					amount += (float) boost.healthBoost;
			player.setHealth(player.getHealth() + amount);
		}
	}

	@Override
	public String writeToString() {
		return name + " (" + healthBoost + ")";
	}

	@Override
	protected SetEffect fromString(String argument) {
		return argument == null ? this : new SetEffectHealth_Boost(Double.parseDouble(argument), false);
	}

	@Override
	protected SetEffect create(Block block) {
		double boost = BlockUtils.getHardness(block);
		if (boost == -1 || boost >= 100) // indestructible
			boost = 100;
		boost = Math.max(boost * 0.4d, 4);
		return new SetEffectHealth_Boost(boost, false);
	}

	@Override
	protected boolean isValid(Block block) {
		if (registryNameContains(block, "bedrock", "obsidian", "brick", "heal", "heart")) {
			float hardness = BlockUtils.getHardness(block);
			return hardness == -1 || hardness >= 2;
		}
		return false;
	}

}

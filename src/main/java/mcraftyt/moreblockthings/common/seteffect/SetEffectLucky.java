package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/**
 * More luck (attribute) and double ore drops when mining. The original used a global loot modifier for this;
 * here {@link BlockDropsEvent} is enough. The looting bonus event of the original no longer exists in NeoForge 26.2
 * (looting is data-driven now), so it is replaced by an approximation (see below).
 */
public class SetEffectLucky extends SetEffect {

	protected SetEffectLucky() {
		super();
		this.color = DARK_GREEN;
		addAttribute(Attributes.LUCK, 3d, AttributeModifier.Operation.ADD_VALUE);
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onDrops);
		bus.addListener(this::onMobDrops);
	}

	/**
	 * Replacement for the looting bonus of the original (the matching event no longer exists): mob drops (not from players)
	 * get roughly one extra item per stack on average, about like Looting II-III.
	 */
	private void onMobDrops(LivingDropsEvent event) {
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getEntity() instanceof Player
				|| !ArmorSet.hasSetEffect(attacker, this))
			return;
		int added = 0;
		for (ItemEntity drop : event.getDrops()) {
			ItemStack stack = drop.getItem();
			if (stack.getMaxStackSize() <= 1)
				continue; // do not multiply armor, tools, etc.
			int extra = event.getEntity().level().getRandom().nextInt(3);
			stack.setCount(Math.min(stack.getCount() + extra, stack.getMaxStackSize()));
			added += extra;
		}
		if (added > 0)
			damageArmor(attacker, 1, false);
	}

	private void onDrops(BlockDropsEvent event) {
		if (!(event.getBreaker() instanceof LivingEntity breaker) || !(breaker.level() instanceof ServerLevel level)
				|| !ArmorSet.hasSetEffect(breaker, this))
			return;
		BlockState state = event.getState();
		if (!state.is(Tags.Blocks.ORES))
			return;
		// do not double if the block itself drops (e.g. iron ore with Silk Touch)
		Item blockItem = state.getBlock().asItem();
		for (ItemEntity drop : event.getDrops())
			if (drop.getItem().getItem() == blockItem)
				return;
		Holder<Enchantment> silkTouch = enchantment(level, Enchantments.SILK_TOUCH);
		if (event.getTool().getAllEnchantments(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)).getLevel(silkTouch) > 0)
			return;
		int before = 0, after = 0;
		for (ItemEntity drop : event.getDrops()) {
			ItemStack stack = drop.getItem();
			before += stack.getCount();
			stack.setCount(Math.min(stack.getCount() * 2, stack.getMaxStackSize()));
			after += stack.getCount();
		}
		if (after > before) {
			doParticlesAndSound(level, event.getPos(), breaker, after - before);
			damageArmor(breaker, after - before, false);
		}
	}

	private void doParticlesAndSound(ServerLevel level, BlockPos pos, LivingEntity entity, float amplifier) {
		level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 5, 0.4f,
				0.4f, 0.4f, 0);
		level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
				0.05f * amplifier, level.getRandom().nextFloat() + 0.9f);
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "emerald", "luck");
	}

}

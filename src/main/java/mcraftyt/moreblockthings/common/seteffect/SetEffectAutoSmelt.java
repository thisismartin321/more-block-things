package mcraftyt.moreblockthings.common.seteffect;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/** Smelts mined blocks and mob drops automatically (key toggles on/off; default: on) */
public class SetEffectAutoSmelt extends SetEffect {

	/** Players who have switched the effect off (not saved, on again after a restart) */
	private static final Set<UUID> DISABLED = new HashSet<>();

	protected SetEffectAutoSmelt() {
		super();
		this.color = DARK_RED;
		this.usesButton = true;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onMobDrops);
		bus.addListener(this::onBlockDrops);
		bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> DISABLED.remove(event.getEntity().getUUID()));
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (SetEffectEvents.isKeyDown(player) && !isOnCooldown(player)) {
			boolean nowDisabled = DISABLED.add(player.getUUID());
			if (!nowDisabled)
				DISABLED.remove(player.getUUID());
			player.sendSystemMessage(Component.literal("AutoSmelt set effect " + (nowDisabled ? "disabled" : "enabled"))
					.withStyle(style -> style.withColor(nowDisabled ? RED : GREEN).withItalic(true)));
			setCooldown(player, 10);
		}
	}

	private boolean isActive(LivingEntity entity) {
		return ArmorSet.hasSetEffect(entity, this) && !DISABLED.contains(entity.getUUID());
	}

	/** Smelt mob drops (not for players, to prevent abuse) */
	private void onMobDrops(LivingDropsEvent event) {
		if (!(event.getEntity().level() instanceof ServerLevel level)
				|| !(event.getSource().getEntity() instanceof LivingEntity attacker) || event.getEntity() instanceof Player
				|| !isActive(attacker))
			return;
		boolean smelted = false;
		for (ItemEntity drop : event.getDrops()) {
			ItemStack result = smelt(drop.getItem(), level);
			if (result != null) {
				smelted = true;
				drop.setItem(result);
			}
		}
		if (smelted) {
			effects(level, event.getEntity().position(), event.getEntity(), 0.3f);
			damageArmor(attacker, 1, false);
		}
	}

	/** Smelt block drops (not with Silk Touch) */
	private void onBlockDrops(BlockDropsEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getBreaker() instanceof LivingEntity breaker)
				|| !isActive(breaker))
			return;
		if (event.getTool().getAllEnchantments(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT))
				.getLevel(enchantment(level, Enchantments.SILK_TOUCH)) > 0)
			return;
		boolean smelted = false;
		for (ItemEntity drop : event.getDrops()) {
			ItemStack result = smelt(drop.getItem(), level);
			if (result != null) {
				smelted = true;
				drop.setItem(result);
			}
		}
		if (smelted) {
			effects(level, Vec3.atCenterOf(event.getPos()), breaker, 0.1f);
			damageArmor(breaker, 1, false);
		}
	}

	private void effects(ServerLevel level, Vec3 pos, LivingEntity entity, float volume) {
		level.sendParticles(ParticleTypes.SMOKE, pos.x() + 0.5f, pos.y() + 0.5f, pos.z() + 0.5f, 10, 0.3f, 0.3f, 0.3f, 0);
		playSound(level, entity.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, volume,
				level.getRandom().nextFloat() + 0.7f);
	}

	/** Smelted form of the item, or null if it cannot be smelted */
	private static ItemStack smelt(ItemStack stack, ServerLevel level) {
		SingleRecipeInput input = new SingleRecipeInput(stack);
		Optional<RecipeHolder<SmeltingRecipe>> recipe = level.recipeAccess().getRecipeFor(RecipeType.SMELTING, input, level);
		if (recipe.isEmpty())
			return null;
		ItemStack result = recipe.get().value().assemble(input);
		if (result.isEmpty())
			return null;
		ItemStack copy = result.copy();
		copy.setCount(stack.getCount() * result.getCount());
		return copy;
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "furnace", "fire", "flame", "smelt", "smoker", "coal")
				&& !registryNameContains(block, "coral");
	}

}

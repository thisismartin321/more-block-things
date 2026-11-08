package mcraftyt.moreblockthings.common.seteffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.config.Config;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/**
 * Base class of all set effects. Greatly simplified compared to 1.18: the effect is called once per tick and player
 * as long as the player wears enough pieces of the set (see {@link Config#piecesForSet}).
 * <ul>
 * <li>Potions: renewed on the server every tick</li>
 * <li>Attributes: as transient modifiers on the player while the effect is active</li>
 * <li>Enchantments: via GetEnchantmentLevelEvent (the items are not modified; applies as long as the piece is a set piece
 * with the effect)</li>
 * </ul>
 */
public abstract class SetEffect {

	// Tooltip colours (RGB, like ChatFormatting)
	protected static final int BLACK = 0x000000, DARK_BLUE = 0x0000AA, DARK_GREEN = 0x00AA00, DARK_AQUA = 0x00AAAA,
			DARK_RED = 0xAA0000, DARK_PURPLE = 0xAA00AA, GOLD = 0xFFAA00, GRAY = 0xAAAAAA, DARK_GRAY = 0x555555,
			BLUE = 0x5555FF, GREEN = 0x55FF55, AQUA = 0x55FFFF, RED = 0xFF5555, LIGHT_PURPLE = 0xFF55FF,
			YELLOW = 0xFFFF55, WHITE = 0xFFFFFF;

	/** All effects, in assignment order */
	public static final List<SetEffect> SET_EFFECTS = new ArrayList<>();
	public static final Map<String, SetEffect> NAME_TO_EFFECT = new HashMap<>();

	// Effects with a key
	public static final SetEffectExplosive EXPLOSIVE = new SetEffectExplosive();
	// Effects without a key
	public static final SetEffectDiving_Suit DIVING_SUIT = new SetEffectDiving_Suit();
	public static final SetEffectFalling FALLING = new SetEffectFalling();
	public static final SetEffectFeeder FEEDER = new SetEffectFeeder();
	public static final SetEffectFlame_Resistant FLAME_RESISTANT = new SetEffectFlame_Resistant();
	public static final SetEffectFrosty FROSTY = new SetEffectFrosty();
	public static final SetEffectInvisibility INVISIBILITY = new SetEffectInvisibility();
	public static final SetEffectMusical MUSICAL = new SetEffectMusical();
	public static final SetEffectPowerful POWERFUL = new SetEffectPowerful();
	public static final SetEffectRegrowth REGROWTH = new SetEffectRegrowth();
	public static final SetEffectSlippery SLIPPERY = new SetEffectSlippery();
	public static final SetEffectSlow_Motion SLOW_MOTION = new SetEffectSlow_Motion();
	public static final SetEffectSoft_Fall SOFT_FALL = new SetEffectSoft_Fall();
	public static final SetEffectSpeedy SPEEDY = new SetEffectSpeedy();
	public static final SetEffectHealth_Boost HEALTH_BOOST = new SetEffectHealth_Boost(4d);
	public static final SetEffectImmovable IMMOVABLE = new SetEffectImmovable(0.25d);
	public static final SetEffectLightweight LIGHTWEIGHT = new SetEffectLightweight();
	public static final SetEffectRocky ROCKY = new SetEffectRocky();
	public static final SetEffectExperience_Giving EXPERIENCE_GIVING = new SetEffectExperience_Giving();
	public static final SetEffectFiery FIERY = new SetEffectFiery();
	public static final SetEffectPrickly PRICKLY = new SetEffectPrickly();
	public static final SetEffectPuller PULLER = new SetEffectPuller();
	public static final SetEffectPusher PUSHER = new SetEffectPusher();
	public static final SetEffectArrow_Defence ARROW_DEFENCE = new SetEffectArrow_Defence();
	public static final SetEffectBonemealer BONEMEALER = new SetEffectBonemealer();
	public static final SetEffectSnowy SNOWY = new SetEffectSnowy();
	public static final SetEffectSleepy SLEEPY = new SetEffectSleepy();
	public static final SetEffectUndying UNDYING = new SetEffectUndying();
	public static final SetEffectRespawn RESPAWN = new SetEffectRespawn();
	public static final SetEffectAbsorbent ABSORBENT = new SetEffectAbsorbent();
	public static final SetEffectIlluminated ILLUMINATED = new SetEffectIlluminated(15);
	public static final SetEffectLucky LUCKY = new SetEffectLucky();
	public static final SetEffectSlimey SLIMEY = new SetEffectSlimey();
	public static final SetEffectCrafter CRAFTER = new SetEffectCrafter();
	public static final SetEffectEnder_Hoarder ENDER_HOARDER = new SetEffectEnder_Hoarder();
	public static final SetEffectEnder ENDER = new SetEffectEnder();
	public static final SetEffectTime_Control TIME_CONTROL = new SetEffectTime_Control();
	public static final SetEffectHoarder HOARDER = new SetEffectHoarder();
	public static final SetEffectAutoSmelt AUTOSMELT = new SetEffectAutoSmelt();

	/** Enchantment this effect gives to an armor piece */
	public record EnchantmentDef(ResourceKey<Enchantment> key, int level, ArmorType slot) {
	}

	private record AttributeDef(Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation operation) {
	}

	/** Name as in the original: class name without "SetEffect", underscores as spaces */
	public final String name;
	/** Colour in the tooltip (RGB) */
	public int color = 0xFFFFFF;
	/** Whether the effect needs the key to be activated */
	public boolean usesButton;

	protected final List<MobEffectInstance> potionEffects = new ArrayList<>();
	public final List<EnchantmentDef> enchantments = new ArrayList<>();
	private final List<AttributeDef> attributes = new ArrayList<>();

	/** End of cooldown per player (game time in ticks); own bookkeeping instead of vanilla item cooldowns */
	private final Map<UUID, Long> cooldownEnd = new HashMap<>();

	protected SetEffect() {
		this(true);
	}

	/** register = false: instance per set (e.g. with block-dependent values), do not register as a "template" */
	protected SetEffect(boolean register) {
		this.name = getClass().getSimpleName().replace("SetEffect", "").replace("_", " ");
		if (register) {
			SET_EFFECTS.add(this);
			NAME_TO_EFFECT.put(this.name, this);
		}
	}

	/** Notation in the config: "Name", for effects with values "Name (value)" */
	public String writeToString() {
		return name;
	}

	/** Instance for the value from the config (the part in brackets, otherwise null). Default: the same instance. */
	protected SetEffect fromString(String argument) {
		return this;
	}

	/** Reads an effect from the config notation, null if unknown */
	public static SetEffect getEffectFromString(String string) {
		if (string == null)
			return null;
		String effectName = string.trim();
		String argument = null;
		int open = effectName.indexOf(" (");
		if (open > 0 && effectName.endsWith(")")) {
			argument = effectName.substring(open + 2, effectName.length() - 1);
			effectName = effectName.substring(0, open);
		}
		SetEffect template = NAME_TO_EFFECT.get(effectName);
		if (template == null)
			return null;
		try {
			return template.fromString(argument);
		} catch (Exception e) {
			return null;
		}
	}

	/** Returns the instance for this block. Effects with block-dependent values return a new instance here. */
	protected SetEffect create(Block block) {
		return this;
	}

	/** Assigns the matching effects to each set (at most one effect with a key per set, as in the original) */
	public static void setup() {
		for (ArmorSet set : ArmorSet.allSets) {
			boolean hasButtonEffect = false;
			set.setEffects = new ArrayList<>();
			for (SetEffect effect : SET_EFFECTS)
				if (effect.isValid(set.block) && !(effect.usesButton && hasButtonEffect)) {
					if (effect.usesButton)
						hasButtonEffect = true;
					set.setEffects.add(effect.create(set.block));
				}
		}
		// Diagnostics: which effects were assigned to how many sets (example names)
		for (SetEffect effect : SET_EFFECTS) {
			List<String> names = new ArrayList<>();
			for (ArmorSet set : ArmorSet.allSets)
				if (set.setEffects.stream().anyMatch(e -> e.name.equals(effect.name)))
					names.add(set.name);
			MoreBlockThings.LOGGER.info("Set effect {}: {} sets {}", effect.name, names.size(),
					names.size() > 8 ? names.subList(0, 8) + "..." : names);
		}
	}

	/** Whether this block should get this effect */
	protected abstract boolean isValid(Block block);

	/** Additional tooltip lines for an armor piece with this effect (e.g. stored items for the Hoarder) */
	public void addTooltip(ItemStack stack, List<Component> lines) {
	}

	/** Effects can register their own game-bus listeners here (called once at startup) */
	public void registerEvents(IEventBus bus) {
	}

	// ============ Tick calls ============

	/** Server: once per tick while the effect is active. Default: apply potions and attributes. */
	public void onTick(ServerPlayer player) {
		for (MobEffectInstance effect : potionEffects)
			if (shouldApplyEffect(effect, player))
				player.addEffect(new MobEffectInstance(effect));
		for (AttributeDef def : attributes) {
			AttributeInstance instance = player.getAttribute(def.attribute());
			if (instance != null && !instance.hasModifier(def.id()))
				instance.addTransientModifier(new AttributeModifier(def.id(), def.amount(), def.operation()));
		}
	}

	/** Server: when the effect is currently NOT active (remove attributes again) */
	public void onInactive(ServerPlayer player) {
		for (AttributeDef def : attributes) {
			AttributeInstance instance = player.getAttribute(def.attribute());
			if (instance != null && instance.hasModifier(def.id()))
				instance.removeModifier(def.id());
		}
	}

	/** Client: once per tick for the local player while the effect is active (movement is client-side) */
	public void onClientTick(Player player) {
	}

	protected boolean shouldApplyEffect(MobEffectInstance effect, Player player) {
		return true;
	}

	protected void addAttribute(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
		Identifier id = Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, key() + "_" + attributes.size());
		attributes.add(new AttributeDef(attribute, id, amount, operation));
	}

	protected void addEnchantment(ResourceKey<Enchantment> key, int level, ArmorType slot) {
		enchantments.add(new EnchantmentDef(key, level, slot));
	}

	/** Helper: registry path of the block contains one of the words */
	protected static boolean registryNameContains(Block block, String... words) {
		String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
		for (String word : words)
			if (path.contains(word))
				return true;
		return false;
	}

	/** Holder of an enchantment from the level's registry */
	protected static Holder<Enchantment> enchantment(Level level, ResourceKey<Enchantment> key) {
		return level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(key);
	}

	/** Plays a sound (overloads for SoundEvent and Holder because both occur in the registries) */
	protected static void playSound(Level level, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
		level.playSound(null, pos, sound, source, volume, pitch);
	}

	protected static void playSound(Level level, BlockPos pos, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
		level.playSound(null, pos, sound.value(), source, volume, pitch);
	}

	// ============ Cooldown ============

	public boolean isOnCooldown(ServerPlayer player) {
		Long end = cooldownEnd.get(player.getUUID());
		return end != null && player.level().getGameTime() < end;
	}

	public void setCooldown(ServerPlayer player, int ticks) {
		cooldownEnd.put(player.getUUID(), player.level().getGameTime() + ticks);
	}

	public void clearState(UUID player) {
		cooldownEnd.remove(player);
	}

	// ============ Durability ============

	/**
	 * Deducts durability from worn pieces with this effect, distributed over the pieces with the most
	 * durability (as in the original). Only if the config demands it or ignoreConfig is set.
	 */
	protected void damageArmor(LivingEntity entity, int amount, boolean ignoreConfig) {
		if ((!ignoreConfig && !Config.effectsUseDurability) || entity == null || entity.level().isClientSide())
			return;
		for (int i = 0; i < amount; i++) {
			ItemStack best = null;
			ArmorType bestType = null;
			for (ArmorType type : ArmorSet.TYPES) {
				ItemStack stack = entity.getItemBySlot(type.getSlot());
				if (stack.getItem() instanceof BlockArmorItem item && item.set.setEffects.contains(this)
						&& !stack.isEmpty() && (best == null
								|| stack.getMaxDamage() - stack.getDamageValue() > best.getMaxDamage() - best.getDamageValue())) {
					best = stack;
					bestType = type;
				}
			}
			if (best == null)
				return;
			best.hurtAndBreak(1, entity, bestType.getSlot());
		}
	}

	// ============ Display ============

	public String key() {
		return name.replace(' ', '_').toLowerCase();
	}

	public Component getDisplayName() {
		return Component.translatable("setEffect." + key() + ".name").withStyle(style -> style.withColor(color));
	}

	/** Description (translation key setEffect.<key>.description) */
	public Component getDescription() {
		return Component.translatable("setEffect." + key() + ".description", getDescriptionObjects());
	}

	/** Additional arguments for the description (e.g. number of hearts) */
	public Object[] getDescriptionObjects() {
		return new Object[0];
	}

	@Override
	public String toString() {
		return name;
	}

}

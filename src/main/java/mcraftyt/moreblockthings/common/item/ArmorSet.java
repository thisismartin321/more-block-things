package mcraftyt.moreblockthings.common.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.config.Config;
import mcraftyt.moreblockthings.common.utils.BlockUtils;

/** An armor set created from a block. Rewritten, inspired by the original (1.18.1). */
public class ArmorSet {

	public static final ArmorType[] TYPES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

	public static final List<ArmorSet> allSets = new ArrayList<>();
	public static final Map<String, ArmorSet> nameToSetMap = new HashMap<>();
	/** Block item -> set, used by the crafting recipe and item lookups */
	public static final Map<Item, ArmorSet> blockItemToSetMap = new HashMap<>();
	/** modid -> sets (alphabetical), needed for the config structure */
	public static final Map<String, TreeSet<ArmorSet>> modidToSetMap = new TreeMap<>();

	/** Blocks that get a set despite the filters (from the original) */
	private static final Set<Block> MANUALLY_ADDED = Set.of(
			Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.DISPENSER, Blocks.DROPPER, Blocks.BEACON, Blocks.FURNACE,
			Blocks.ENCHANTING_TABLE, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK,
			Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.SOUL_SAND, Blocks.ENDER_CHEST,
			Blocks.BLAST_FURNACE, Blocks.COMPOSTER, Blocks.SMOKER, Blocks.BEE_NEST, Blocks.BEEHIVE, Blocks.BARREL,
			Blocks.HONEY_BLOCK, Blocks.CHEST, Blocks.NOTE_BLOCK, Blocks.JUKEBOX);

	private static final Set<Block> BAD_BLOCKS = Set.of(
			Blocks.BARRIER, Blocks.IRON_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.AIR, Blocks.SNOW,
			Blocks.NETHERITE_BLOCK);

	/** Modded blocks that were excluded in the original (lowercase) */
	private static final Set<String> BAD_IDS = Set.of(
			"evilcraft:darkblock", "evilcraft:obscuredglass", "evilcraft:hardenedblood", "evilcraft:darkpowergemblock",
			"darkutils:filter", "darkutils:filter_inverted", "agriculturalrevolution:rustedanalyser",
			"agriculturalrevolution:rustedbot", "agriculturalrevolution:rustedmutationstation",
			"agriculturalrevolution:rustedinscriber", "agriculturalrevolution:rustedhydrophonic",
			"agriculturalrevolution:rustedresearch", "agriculturalrevolution:rustedpipe",
			"agriculturalrevolution:rustedironscaff", "tconstruct:clear_glass");

	/** All sets share one equipment asset; model and texture come from the client extensions */
	public static final ResourceKey<EquipmentAsset> SHARED_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
			Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "block_armor"));

	public final Item blockItem;
	public final Block block;
	/** Path of the block item, e.g. "stone"; serves as the base name of the armor items */
	public final String name;
	public final String modid;
	public final boolean isFromModdedBlock;
	public final ResourceKey<EquipmentAsset> assetKey;

	// values computed from the block, overridden by the config afterwards
	public float damageReduction;
	public float toughness;
	public int durability;
	public int enchantability;
	public float knockbackResistance;
	public boolean enabled = true;

	/** Effects of this set (assigned by SetEffect#setup) */
	public List<mcraftyt.moreblockthings.common.seteffect.SetEffect> setEffects = new ArrayList<>();

	public ArmorMaterial material;
	public final Map<ArmorType, BlockArmorItem> pieces = new EnumMap<>(ArmorType.class);

	public ArmorSet(BlockItem item) {
		this.blockItem = item;
		this.block = item.getBlock();
		Identifier id = BuiltInRegistries.ITEM.getKey(item);
		this.name = id.getPath().toLowerCase().replace(' ', '_');
		this.modid = id.getNamespace().toLowerCase();
		this.isFromModdedBlock = !this.modid.equals("minecraft");
		this.assetKey = SHARED_ASSET;

		// Compute values from block properties (formulas unchanged from the original)
		float rawHardness = BlockUtils.getHardness(block);
		boolean indestructible = rawHardness == -1;
		float hardness = Math.min(rawHardness, 100);
		if (indestructible)
			hardness = 100;
		float blastResistance = BlockUtils.getBlastResistance(block);
		boolean requiresTool = BlockUtils.getRequiresTool(block);
		boolean isSolid = BlockUtils.getIsSolid(block);

		float reduction = hardness >= 8 ? (hardness * 0.003f + 4.5f) : (hardness * 0.65f);
		if (requiresTool)
			reduction *= 1.2f;
		if (!isSolid)
			reduction *= 0.5f;
		if (indestructible)
			reduction *= 20f;
		this.damageReduction = Math.max(reduction * 0.6f, 1);
		this.durability = (int) (indestructible ? 0 : Math.min(30, damageReduction * 8f));
		this.toughness = Math.min(20, blastResistance > 100 ? blastResistance / 400f : 0);
		this.enchantability = 12;
		this.knockbackResistance = 0; // 0, because set effects provide knockback resistance
	}

	/** (Re)creates the material from the current values, including the global config multipliers */
	public void createMaterial() {
		float dr = damageReduction * (float) Config.globalDamageReductionModifier;
		Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
		defense.put(ArmorType.HELMET, (int) dr);
		defense.put(ArmorType.CHESTPLATE, (int) (dr * 2f));
		defense.put(ArmorType.LEGGINGS, (int) (dr * 2.5f));
		defense.put(ArmorType.BOOTS, (int) (dr * 1.45f));
		defense.put(ArmorType.BODY, 0);
		this.material = new ArmorMaterial(
				(int) (durability * Config.globalDurabilityModifier),
				defense,
				(int) (enchantability * Config.globalEnchantabilityModifier),
				SoundEvents.ARMOR_EQUIP_GENERIC,
				toughness * (float) Config.globalToughnessModifier,
				knockbackResistance * (float) Config.globalKnockbackResistanceModifier,
				// Placeholder tag; the actual repair item (the block itself) is set via Item.Properties#repairable
				TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "repairs/" + name)),
				assetKey);
	}

	public BlockArmorItem getPiece(ArmorType type) {
		return pieces.get(type);
	}

	// ============ Set creation ============

	/** Creates a set for every valid BlockItem. Must run in a late item RegisterEvent. */
	public static void setup() {
		allSets.clear();
		nameToSetMap.clear();
		blockItemToSetMap.clear();
		modidToSetMap.clear();

		// Collect items beforehand because the registry changes during registration
		List<BlockItem> candidates = new ArrayList<>();
		for (Item item : BuiltInRegistries.ITEM)
			if (item instanceof BlockItem blockItem && isValid(blockItem))
				candidates.add(blockItem);
		// deterministic order so that registration stays stable
		candidates.sort(Comparator.comparing((BlockItem i) -> BuiltInRegistries.ITEM.getKey(i).toString()));

		Set<String> usedNames = new HashSet<>();
		for (BlockItem item : candidates) {
			try {
				ArmorSet set = new ArmorSet(item);
				if (set.name.isEmpty() || !usedNames.add(set.name))
					continue; // Duplicate names (same path in another mod): the first one wins, as in the original
				allSets.add(set);
				nameToSetMap.put(set.name, set);
				blockItemToSetMap.put(item, set);
				modidToSetMap.computeIfAbsent(set.modid,
						k -> new TreeSet<>(Comparator.comparing((ArmorSet s) -> s.name))).add(set);
			} catch (Exception e) {
				MoreBlockThings.LOGGER.warn("Could not create a set for {}", item, e);
			}
		}
	}

	/**
	 * Effects a living entity currently has active: for every set of which at least
	 * {@link Config#piecesForSet} pieces are worn, all effects of the set count.
	 */
	public static Set<mcraftyt.moreblockthings.common.seteffect.SetEffect> getWornSetEffects(net.minecraft.world.entity.LivingEntity entity) {
		Set<mcraftyt.moreblockthings.common.seteffect.SetEffect> effects = new HashSet<>();
		Map<ArmorSet, Integer> counts = new HashMap<>();
		for (ArmorType type : TYPES) {
			net.minecraft.world.item.ItemStack stack = entity.getItemBySlot(type.getSlot());
			if (stack.getItem() instanceof BlockArmorItem item && item.set.enabled)
				counts.merge(item.set, 1, Integer::sum);
		}
		for (Map.Entry<ArmorSet, Integer> entry : counts.entrySet())
			if (entry.getValue() >= Config.piecesForSet)
				effects.addAll(entry.getKey().setEffects);
		return effects;
	}

	/** Whether the living entity currently wears an active set with this effect */
	public static boolean hasSetEffect(net.minecraft.world.entity.LivingEntity entity, mcraftyt.moreblockthings.common.seteffect.SetEffect effect) {
		return entity != null && getWornSetEffects(entity).contains(effect);
	}

	public static ArmorSet getSet(Block block) {
		for (ArmorSet set : allSets)
			if (set.block == block)
				return set;
		return null;
	}

	public static ArmorSet getSet(Item item) {
		if (item instanceof BlockArmorItem armor)
			return armor.set;
		return blockItemToSetMap.get(item);
	}

	/** Should this item become a set? Display-name filters of the original are dropped because translations are not available at registration time. */
	private static boolean isValid(BlockItem item) {
		try {
			Block block = item.getBlock();
			Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
			Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
			boolean vanilla = blockId.getNamespace().equals("minecraft");

			// vanilla shulker boxes and beds: allowed manually (own textures)
			if (vanilla && (block instanceof ShulkerBoxBlock || block instanceof BedBlock))
				return true;
			if (MANUALLY_ADDED.contains(block))
				return true;

			String path = itemId.getPath();
			String ns = itemId.getNamespace();
			if (ns.contains("one_point_twelve_concrete") || ns.contains("railcraft") || path.contains("ore")
					|| path.contains("ingot"))
				return false;

			BlockState state = block.defaultBlockState();
			if (block instanceof LiquidBlock || block instanceof BaseEntityBlock || block instanceof CropBlock
					|| block instanceof BushBlock || block instanceof SlabBlock || block instanceof InfestedBlock
					|| state.is(Tags.Blocks.ORES) || BAD_BLOCKS.contains(block)
					|| state.getRenderShape() != RenderShape.MODEL)
				return false;
			if (BAD_IDS.contains(blockId.toString().toLowerCase()))
				return false;

			// full blocks only
			return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		} catch (Exception e) {
			return false;
		}
	}

}

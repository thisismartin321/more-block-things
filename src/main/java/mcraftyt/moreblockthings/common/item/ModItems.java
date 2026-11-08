package mcraftyt.moreblockthings.common.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.config.Config;

public class ModItems {

	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, MoreBlockThings.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_TABS.register("moreblockthings",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup." + MoreBlockThings.MODID))
					.icon(() -> {
						// Stone helmet as icon, otherwise the first available piece, otherwise an iron helmet
						ArmorSet set = ArmorSet.nameToSetMap.getOrDefault("stone",
								ArmorSet.allSets.isEmpty() ? null : ArmorSet.allSets.get(0));
						return set != null && set.getPiece(ArmorType.HELMET) != null
								? new ItemStack(set.getPiece(ArmorType.HELMET))
								: new ItemStack(Items.IRON_HELMET);
					})
					.displayItems((params, output) -> {
						for (ArmorSet set : ArmorSet.allSets)
							if (set.enabled)
								for (ArmorType type : ArmorSet.TYPES)
									if (set.getPiece(type) != null)
										output.accept(set.getPiece(type));
					}).build());

	/**
	 * Runs with lowest priority on the mod bus. Unlike in 1.18 no registry unfreeze hack is needed:
	 * the item RegisterEvent is open, and all block registrations of all mods are already done.
	 */
	public static void onRegister(RegisterEvent event) {
		if (!event.getRegistryKey().equals(Registries.ITEM))
			return;

		// 1. Form sets from all BlockItems registered so far
		ArmorSet.setup();
		mcraftyt.moreblockthings.common.seteffect.SetEffect.setup();
		// Register the effects' game-bus listeners only here: the effect classes access vanilla registries when loaded
		mcraftyt.moreblockthings.common.seteffect.SetEffectEvents.register(net.neoforged.neoforge.common.NeoForge.EVENT_BUS);

		// 2. Build and register the config with options per set (STARTUP is read immediately)
		ModConfigSpec spec = Config.build();
		MoreBlockThings.container.registerConfig(ModConfig.Type.STARTUP, spec);
		Config.apply();

		// 3. Create the materials with the config values and register the items
		int vanilla = 0, modded = 0;
		for (ArmorSet set : ArmorSet.allSets)
			set.createMaterial();

		event.register(Registries.ITEM, helper -> {
			for (ArmorSet set : ArmorSet.allSets) {
				for (ArmorType type : ArmorSet.TYPES) {
					Identifier id = Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, set.name + "_" + suffix(type));
					ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
					Item.Properties props = new Item.Properties()
							.setId(key)
							.humanoidArmor(set.material, type)
							.durability(type.getDurability(set.material.durability()))
							.repairable(set.blockItem);
					BlockArmorItem item = new BlockArmorItem(props, set, type);
					set.pieces.put(type, item);
					helper.register(id, item);
				}
			}
		});

		for (ArmorSet set : ArmorSet.allSets)
			if (set.isFromModdedBlock)
				modded += 4;
			else
				vanilla += 4;
		MoreBlockThings.LOGGER.info("Generated {} Block Armor items from Vanilla blocks", vanilla);
		if (modded > 0)
			MoreBlockThings.LOGGER.info("Generated {} Block Armor items from Modded blocks", modded);
	}

	/**
	 * Item.Properties sets ITEM_NAME only AFTER its own components (from the description ID) and thereby overwrites
	 * a name from the properties. That is why we set "<Block> Helmet" etc. via this event, which runs afterwards.
	 */
	public static void onModifyComponents(ModifyDefaultComponentsEvent event) {
		for (ArmorSet set : ArmorSet.allSets)
			for (ArmorType type : ArmorSet.TYPES) {
				Item piece = set.getPiece(type);
				if (piece != null)
					event.modify(piece, (components, context, key) -> {
						components.set(DataComponents.ITEM_NAME, Component.translatable("item." + MoreBlockThings.MODID + "." + suffix(type),
								Component.translatable(set.block.getDescriptionId())));
						// all pieces share the dynamic icon model (assets/moreblockthings/items/block_armor.json)
						components.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "block_armor"));
					});
			}
	}

	private static String suffix(ArmorType type) {
		return switch (type) {
			case HELMET -> "helmet";
			case CHESTPLATE -> "chestplate";
			case LEGGINGS -> "leggings";
			case BOOTS -> "boots";
			default -> "body";
		};
	}

}

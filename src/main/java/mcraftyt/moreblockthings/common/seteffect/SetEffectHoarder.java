package mcraftyt.moreblockthings.common.seteffect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/**
 * Every worn piece stores items (helmet/boots 9, chestplate/leggings 18 slots); the key opens a chest with
 * all slots. Storage uses the vanilla component CONTAINER (like shulker boxes) instead of NBT.
 * If a piece breaks, its stored items drop out.
 */
public class SetEffectHoarder extends SetEffect {

	protected SetEffectHoarder() {
		super();
		this.color = GOLD;
		this.usesButton = true;
	}

	private static int sizeOf(ArmorType type) {
		return switch (type) {
			case HELMET, BOOTS -> 9;
			default -> 18;
		};
	}

	/** Worn pieces whose set has this effect (in the order helmet, chestplate, leggings, boots) */
	private static List<ArmorType> hoarderTypes(Player player) {
		List<ArmorType> types = new ArrayList<>();
		for (ArmorType type : ArmorSet.TYPES)
			if (player.getItemBySlot(type.getSlot()).getItem() instanceof BlockArmorItem item
					&& item.set.setEffects.stream().anyMatch(e -> e instanceof SetEffectHoarder))
				types.add(type);
		return types;
	}

	private static boolean isHoarderItem(ItemStack stack) {
		return stack.getItem() instanceof BlockArmorItem item
				&& item.set.setEffects.stream().anyMatch(e -> e instanceof SetEffectHoarder);
	}

	public static NonNullList<ItemStack> getStoredItems(ItemStack piece, ArmorType type) {
		NonNullList<ItemStack> items = NonNullList.withSize(sizeOf(type), ItemStack.EMPTY);
		piece.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
		return items;
	}

	@Override
	public void registerEvents(IEventBus bus) {
		bus.addListener(this::onItemBroken);
	}

	/** If a Hoarder piece breaks, the stored items drop out (and an open chest is closed) */
	private void onItemBroken(PlayerDestroyItemEvent event) {
		ItemStack original = event.getOriginal();
		if (!(event.getEntity() instanceof ServerPlayer player) || !(original.getItem() instanceof BlockArmorItem item)
				|| !isHoarderItem(original))
			return;
		for (ItemStack stored : getStoredItems(original, item.type))
			if (!stored.isEmpty())
				player.drop(stored.copy(), false);
		if (player.containerMenu instanceof ChestMenu)
			player.closeContainer();
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!SetEffectEvents.isKeyDown(player) || isOnCooldown(player))
			return;
		List<ArmorType> types = hoarderTypes(player);
		int size = 0;
		for (ArmorType type : types)
			size += sizeOf(type);
		int rows = size / 9;
		if (rows < 1)
			return;
		setCooldown(player, 20);

		// merge the contents of all pieces
		SimpleContainer container = new SimpleContainer(size) {
			@Override
			public boolean canPlaceItem(int slot, ItemStack stack) {
				return !isHoarderItem(stack); // no Hoarder pieces inside Hoarder pieces (prevents infinite storage)
			}

			/** Writes changes back into the pieces immediately */
			@Override
			public void setChanged() {
				int index = 0;
				for (ArmorType type : types) {
					ItemStack piece = player.getItemBySlot(type.getSlot());
					List<ItemStack> part = new ArrayList<>();
					for (int i = 0; i < sizeOf(type); i++)
						part.add(getItem(index + i).copy());
					if (piece.getItem() instanceof BlockArmorItem)
						piece.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(part));
					index += sizeOf(type);
				}
				super.setChanged();
			}
		};
		int index = 0;
		for (ArmorType type : types) {
			NonNullList<ItemStack> stored = getStoredItems(player.getItemBySlot(type.getSlot()), type);
			for (int i = 0; i < stored.size(); i++)
				container.setItem(index + i, stored.get(i));
			index += sizeOf(type);
		}

		playSound(player.level(), player.blockPosition(), getOpenSound(player), SoundSource.PLAYERS, 0.6f, 1f);
		player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ChestMenu(menuType(rows), id, inventory, container, rows),
				Component.literal("Hoarder Storage")));
		damageArmor(player, 1, false);
	}

	private static MenuType<ChestMenu> menuType(int rows) {
		return switch (rows) {
			case 1 -> MenuType.GENERIC_9x1;
			case 2 -> MenuType.GENERIC_9x2;
			case 3 -> MenuType.GENERIC_9x3;
			case 4 -> MenuType.GENERIC_9x4;
			case 5 -> MenuType.GENERIC_9x5;
			default -> MenuType.GENERIC_9x6;
		};
	}

	/** Opening sound matching the block (barrel, shulker box, otherwise chest) */
	private static net.minecraft.sounds.SoundEvent getOpenSound(Player player) {
		for (ArmorType type : ArmorSet.TYPES)
			if (player.getItemBySlot(type.getSlot()).getItem() instanceof BlockArmorItem item
					&& item.set.setEffects.stream().anyMatch(e -> e instanceof SetEffectHoarder)) {
				if (item.set.block instanceof BarrelBlock)
					return SoundEvents.BARREL_OPEN;
				if (item.set.block instanceof ShulkerBoxBlock)
					return SoundEvents.SHULKER_BOX_OPEN;
			}
		return SoundEvents.CHEST_OPEN;
	}

	/** Up to four stored items in the tooltip */
	@Override
	public void addTooltip(ItemStack stack, List<Component> lines) {
		if (!(stack.getItem() instanceof BlockArmorItem item))
			return;
		List<ItemStack> nonEmpty = new ArrayList<>();
		for (ItemStack stored : getStoredItems(stack, item.type))
			if (!stored.isEmpty())
				nonEmpty.add(stored);
		for (int i = 0; i < 4 && i < nonEmpty.size(); i++) {
			ItemStack stored = nonEmpty.get(i);
			lines.add(Component.literal("  - ").append(stored.getHoverName().copy().withStyle(style -> style.withColor(color)))
					.append(" x" + stored.getCount()));
		}
		if (nonEmpty.size() > 4)
			lines.add(Component.literal("  ").append(Component.translatable("container.shulkerBox.more", nonEmpty.size() - 4))
					.withStyle(style -> style.withColor(color).withItalic(true)));
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "chest", "shulker", "storage", "crate", "barrel") || block instanceof ChestBlock
				|| block instanceof ShulkerBoxBlock || block instanceof BarrelBlock;
	}

}

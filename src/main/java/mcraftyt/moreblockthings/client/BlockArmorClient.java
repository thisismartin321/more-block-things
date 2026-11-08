package mcraftyt.moreblockthings.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/**
 * Hooks the custom armor model and the block texture in through the NeoForge client extensions.
 * <p>
 * The texture per body part is the real side sprite of the block model (as in the original:
 * top = helmet, front (NORTH) = chestplate, back (SOUTH) = leggings, bottom = boots). Rendering uses
 * the block atlas as the texture; the UVs of each face are converted into the sprite's area.
 * <p>
 * Known limitation: animated sprites show only their first frame.
 * Special-case textures (chests, cactus, beds, ...) come from TextureOverrides.
 */
public class BlockArmorClient {

	private record Key(ArmorSet set, ArmorType type) {
	}

	private static final Map<Key, HumanoidModel<HumanoidRenderState>> MODELS = new HashMap<>();
	/** Sprite of a block side and its tint index (-1 = no tint) */
	/** fixedTint != 0: fixed tint (overrides), otherwise tint via the tint index of the block model */
	record Side(TextureAtlasSprite sprite, int tintIndex, int fixedTint) {
	}

	private static final Map<ArmorSet, Map<ArmorType, Side>> SPRITES = new HashMap<>();

	public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
		Extensions extensions = new Extensions();
		for (ArmorSet set : ArmorSet.allSets)
			for (ArmorType type : ArmorSet.TYPES) {
				Item piece = set.getPiece(type);
				if (piece != null)
					event.registerItem(extensions, piece);
			}
	}

	/** Registers the custom item model type "moreblockthings:icon" for the inventory icons */
	public static void onRegisterItemModels(net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) {
		event.register(Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "icon"), BlockArmorIconModel.Unbaked.MAP_CODEC);
	}

	/** The atlas coordinates change after every resource reload, so clear the caches */
	public static void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
		MODELS.clear();
		SPRITES.clear();
		BlockArmorIcons.clear();
	}

	/** Finds the sprite of the matching block side for each body part (logic from ArmorSet#initTextures of the original) */
	/** Sprite and tint index of the block side for a body part (null if it cannot be determined) */
	static Side getSide(ArmorSet set, ArmorType type) {
		return getSprites(set).get(type);
	}

	private static Map<ArmorType, Side> getSprites(ArmorSet set) {
		return SPRITES.computeIfAbsent(set, s -> {
			Map<ArmorType, Side> map = new EnumMap<>(ArmorType.class);
			try {
				BlockState state = s.block.defaultBlockState();
				if (s.block == Blocks.REDSTONE_LAMP)
					state = state.setValue(RedstoneLampBlock.LIT, true);

				BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
				List<BlockStateModelPart> parts = new ArrayList<>();
				model.collectParts(BlockAndTintGetter.EMPTY, BlockPos.ZERO, state, RandomSource.create(), parts);

				TextureAtlasSprite particle = null;
				for (BlockStateModelPart part : parts) {
					if (particle == null)
						particle = part.particleMaterial().sprite();
					List<Direction> directions = new ArrayList<>();
					directions.add(null); // Quads without a culling side
					for (Direction d : Direction.values())
						directions.add(d);
					for (Direction side : directions)
						for (BakedQuad quad : part.getQuads(side)) {
							TextureAtlasSprite sprite = quad.materialInfo().sprite();
							if (sprite.contents().name().toString().contains("overlay"))
								continue; // Overlays are ignored (as in the original)
							ArmorType type = switch (quad.direction()) {
								case UP -> ArmorType.HELMET;
								case NORTH -> ArmorType.CHESTPLATE;
								case SOUTH -> ArmorType.LEGGINGS;
								case DOWN -> ArmorType.BOOTS;
								default -> null;
							};
							if (type != null)
								map.put(type, new Side(sprite, quad.materialInfo().tintIndex(), 0));
						}
				}
				// Texture overrides of the original (chests, cactus, beds, ...) take priority
				applyOverrides(s, map);
				// If a side is missing, use the block's particle sprite
				if (particle != null)
					for (ArmorType type : ArmorSet.TYPES)
						map.putIfAbsent(type, new Side(particle, -1, 0));
			} catch (Exception e) {
				MoreBlockThings.LOGGER.warn("Could not determine textures for {}", s.name, e);
			}
			return map;
		});
	}

	/** Replaces sides with the override sprites from the item atlas (textures/item/override/*.png) */
	private static void applyOverrides(ArmorSet set, Map<ArmorType, Side> map) {
		TextureAtlas items = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(Identifier.withDefaultNamespace("items"));
		for (ArmorType type : ArmorSet.TYPES) {
			TextureOverrides.Info info = TextureOverrides.get(set.block, type);
			if (info == null)
				continue;
			TextureAtlasSprite sprite = items
					.getSprite(Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "item/override/" + info.texture()));
			if (sprite != null && !sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation()))
				map.put(type, new Side(sprite, -1, info.tint()));
			else
				MoreBlockThings.LOGGER.warn("Override texture {} for {} not found", info.texture(), set.name);
		}
	}

	private static HumanoidModel<HumanoidRenderState> getModel(ArmorSet set, ArmorType type) {
		return MODELS.computeIfAbsent(new Key(set, type), k -> {
			Side side = getSprites(set).get(type);
			TextureAtlasSprite sprite = side == null ? null : side.sprite();
			ModelPart root = sprite != null
					? BlockArmorGeometry.buildRoot(type, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1())
					: BlockArmorGeometry.buildRoot(type, 0, 0, 1, 1); // Fallback: whole loose texture
			return new HumanoidModel<>(root);
		});
	}

	private static Identifier getTexture(ArmorSet set, ArmorType type) {
		Side side = getSprites(set).get(type);
		if (side != null && side.sprite() != null)
			return side.sprite().atlasLocation();
		// Fallback: loose PNG by registry name
		Identifier id = BuiltInRegistries.BLOCK.getKey(set.block);
		return Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/block/" + id.getPath() + ".png");
	}

	/** Default tint of the block side (like the item colour in the original): grass, leaves, ... */
	static int getTint(ArmorSet set, ArmorType type) {
		Side side = getSprites(set).get(type);
		if (side != null && side.fixedTint() != 0)
			return side.fixedTint();
		if (side == null || side.tintIndex() < 0)
			return 0; // 0 = not tinted
		try {
			BlockTintSource source = Minecraft.getInstance().getBlockColors()
					.getTintSource(set.block.defaultBlockState(), side.tintIndex());
			return source == null ? 0 : (source.color(set.block.defaultBlockState()) | 0xFF000000);
		} catch (Exception e) {
			return 0;
		}
	}

	private static class Extensions implements IClientItemExtensions {

		@Override
		public int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor) {
			if (stack.getItem() instanceof BlockArmorItem item) {
				int tint = getTint(item.set, item.type);
				if (tint != 0)
					return tint;
			}
			return IClientItemExtensions.super.getArmorLayerTintColor(stack, layer, layerIdx, fallbackColor);
		}

		@Override
		public HumanoidModel<?> getHumanoidArmorModel(ItemStack stack, EquipmentClientInfo.LayerType layerType, Model original) {
			if (stack.getItem() instanceof BlockArmorItem item)
				return getModel(item.set, item.type);
			return original instanceof HumanoidModel<?> h ? h : null;
		}

		@Override
		public Identifier getArmorTexture(ItemStack stack, EquipmentClientInfo.LayerType type, EquipmentClientInfo.Layer layer, Identifier fallback) {
			if (stack.getItem() instanceof BlockArmorItem item)
				return getTexture(item.set, item.type);
			return null;
		}

	}

}

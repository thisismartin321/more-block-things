package mcraftyt.moreblockthings.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.google.common.base.Suppliers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.ArmorType;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.item.ArmorSet;

/**
 * Creates the inventory icons: the shape of the armor piece (template textures, two halves) is filled pixel by pixel
 * with the block texture, as in the original (ItemTextureQuadConverter). The base layer is rendered by vanilla as a
 * normal "generated" model; fill and cover are created here and sit slightly in front of the base so that they do
 * not overlap it in 3D (hand, ground) ("z-fighting").
 */
public class BlockArmorIcons {

	/** Fill layer (block atlas, possibly tinted) and cover layer (item atlas) */
	public record IconData(List<BakedQuad> fill, Supplier<Vector3fc[]> fillExtents, int tint, List<BakedQuad> cover,
			Supplier<Vector3fc[]> coverExtents) {
	}

	private record Key(ArmorSet set, ArmorType type) {
	}

	private static final IconData EMPTY = new IconData(List.of(), () -> new Vector3fc[0], 0, List.of(), () -> new Vector3fc[0]);
	private static final Map<Key, IconData> CACHE = new HashMap<>();

	// Plane of the front/back like vanilla "generated" models (7.5 to 8.5 of 16), plus a small gap per layer
	private static final float SOUTH_Z = 8.5f / 16f;
	private static final float NORTH_Z = 7.5f / 16f;
	private static final float FILL_OFFSET = 0.001f;
	private static final float COVER_OFFSET = 0.002f;
	/** How far (in texels) sampling moves into the interior of a template pixel so that neighbouring sprites do not "bleed" through */
	private static final float UV_INSET = 0.25f;

	public static void clear() {
		CACHE.clear();
	}

	public static String typeName(ArmorType type) {
		return switch (type) {
			case HELMET -> "helmet";
			case CHESTPLATE -> "chestplate";
			case LEGGINGS -> "leggings";
			case BOOTS -> "boots";
			default -> "body";
		};
	}

	public static IconData get(ArmorSet set, ArmorType type) {
		return CACHE.computeIfAbsent(new Key(set, type), k -> build(set, type));
	}

	private static IconData build(ArmorSet set, ArmorType type) {
		try {
			TextureAtlas items = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(Identifier.withDefaultNamespace("items"));
			BlockArmorClient.Side side = BlockArmorClient.getSide(set, type);
			if (side == null || side.sprite() == null)
				return EMPTY;
			TextureAtlasSprite block = side.sprite();
			int tint = BlockArmorClient.getTint(set, type);
			int tintIndex = tint != 0 ? 0 : -1;
			BakedQuad.MaterialInfo info = BakedQuad.MaterialInfo.of(new Material.Baked(block, false), block.transparency(),
					tintIndex, true, 0, true);
			List<BakedQuad> fill = new ArrayList<>();
			for (int half = 1; half <= 2; half++) {
				TextureAtlasSprite template = items.getSprite(
						Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "item/icon/" + typeName(type) + half + "_template"));
				int w = template.contents().width(), h = template.contents().height();
				for (int y = 0; y < h; y++)
					for (int x = 0; x < w; x++) {
						if (((template.getPixelRGBA(0, x, y) >>> 24) & 0xFF) == 0)
							continue; // only pixels that lie within the armor piece's shape
						addPixel(fill, info, block, x, y, w, h);
					}
			}

			// Cover: one quad at the front and one at the back with the cover texture
			List<BakedQuad> cover = new ArrayList<>();
			TextureAtlasSprite coverSprite = items
					.getSprite(Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "item/icon/" + typeName(type) + "_cover"));
			BakedQuad.MaterialInfo coverInfo = BakedQuad.MaterialInfo.of(new Material.Baked(coverSprite, false),
					coverSprite.transparency(), -1, true, 0, true);
			addFull(cover, coverInfo, coverSprite);

			return new IconData(fill, Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(fill)), tint, cover,
					Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(cover)));
		} catch (Exception e) {
			MoreBlockThings.LOGGER.warn("Could not create icon for {}", set.name, e);
			return EMPTY;
		}
	}

	/** One pixel of the template: front and back, filled with the same pixel of the block texture */
	private static void addPixel(List<BakedQuad> quads, BakedQuad.MaterialInfo info, TextureAtlasSprite block, int x, int y, int w, int h) {
		float x0 = (float) x / w, x1 = (x + 1f) / w;
		float yTop = 1f - (float) y / h, yBottom = 1f - (y + 1f) / h;
		// Sampling area moved inwards (no bleeding over the sprite's edge)
		float u0 = block.getU((x + UV_INSET) / w), u1 = block.getU((x + 1f - UV_INSET) / w);
		float v0 = block.getV((y + UV_INSET) / h), v1 = block.getV((y + 1f - UV_INSET) / h);
		addQuads(quads, info, x0, x1, yTop, yBottom, u0, u1, v0, v1, FILL_OFFSET);
	}

	/** The whole 16x16 quad for the cover texture */
	private static void addFull(List<BakedQuad> quads, BakedQuad.MaterialInfo info, TextureAtlasSprite sprite) {
		float eps = 0.1f / Math.max(1, sprite.contents().width());
		addQuads(quads, info, 0f, 1f, 1f, 0f, sprite.getU(eps), sprite.getU(1f - eps), sprite.getV(eps), sprite.getV(1f - eps),
				COVER_OFFSET);
	}

	private static void addQuads(List<BakedQuad> quads, BakedQuad.MaterialInfo info, float x0, float x1, float yTop, float yBottom,
			float u0, float u1, float v0, float v1, float zOffset) {
		float south = SOUTH_Z + zOffset, north = NORTH_Z - zOffset;
		// Front (SOUTH): top left, bottom left, bottom right, top right
		quads.add(new BakedQuad(new Vector3f(x0, yTop, south), new Vector3f(x0, yBottom, south),
				new Vector3f(x1, yBottom, south), new Vector3f(x1, yTop, south), UVPair.pack(u0, v0), UVPair.pack(u0, v1),
				UVPair.pack(u1, v1), UVPair.pack(u1, v0), Direction.SOUTH, info));
		// Back (NORTH): mirrored
		quads.add(new BakedQuad(new Vector3f(x1, yTop, north), new Vector3f(x1, yBottom, north),
				new Vector3f(x0, yBottom, north), new Vector3f(x0, yTop, north), UVPair.pack(u1, v0), UVPair.pack(u1, v1),
				UVPair.pack(u0, v1), UVPair.pack(u0, v0), Direction.NORTH, info));
	}

}

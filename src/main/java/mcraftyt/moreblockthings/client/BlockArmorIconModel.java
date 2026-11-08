package mcraftyt.moreblockthings.client;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/**
 * Item model of the armor pieces with three layers: base (vanilla "generated" model), form dynamically filled
 * from the block texture (see {@link BlockArmorIcons}) and cover (vanilla "generated" model).
 */
public class BlockArmorIconModel implements ItemModel {

	/** Static layers and display properties (transforms for hand, ground, ...) per armor type */
	private record Part(ItemModel base, ModelRenderProperties properties, Matrix4fc transformation) {
	}

	private final Map<ArmorType, Part> parts;

	private BlockArmorIconModel(Map<ArmorType, Part> parts) {
		this.parts = parts;
	}

	@Override
	public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext,
			@Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		output.appendModelIdentityElement(this);
		if (!(item.getItem() instanceof BlockArmorItem armor))
			return;
		Part part = parts.get(armor.type);
		if (part == null)
			return;

		// 1. Base
		part.base().update(output, item, resolver, displayContext, level, owner, seed);

		// 2. Form, filled with the block texture, and 3. cover (each its own layer, slightly in front of the base)
		BlockArmorIcons.IconData data = BlockArmorIcons.get(armor.set, armor.type);
		output.appendModelIdentityElement(armor.set);
		output.appendModelIdentityElement(armor.type);
		output.appendModelIdentityElement(data.tint());
		if (!data.fill().isEmpty()) {
			ItemStackRenderState.LayerRenderState layer = output.newLayer();
			if (data.tint() != 0)
				layer.tintLayers().add(data.tint());
			layer.setExtents(data.fillExtents());
			layer.setLocalTransform(part.transformation());
			part.properties().applyToLayer(layer, displayContext);
			layer.prepareQuadList().addAll(data.fill());
		}
		if (!data.cover().isEmpty()) {
			ItemStackRenderState.LayerRenderState layer = output.newLayer();
			layer.setExtents(data.coverExtents());
			layer.setLocalTransform(part.transformation());
			part.properties().applyToLayer(layer, displayContext);
			layer.prepareQuadList().addAll(data.cover());
		}
	}

	public record Unbaked(Identifier prefix) implements ItemModel.Unbaked {

		public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder
				.mapCodec(i -> i.group(Identifier.CODEC.fieldOf("prefix").forGetter(Unbaked::prefix)).apply(i, Unbaked::new));

		private Identifier model(ArmorType type, String kind) {
			return Identifier.fromNamespaceAndPath(prefix.getNamespace(),
					prefix.getPath() + "/" + BlockArmorIcons.typeName(type) + "_" + kind);
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			for (ArmorType type : ArmorSet.TYPES) {
				resolver.markDependency(model(type, "base"));
			}
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
			ModelBaker baker = context.blockModelBaker();
			Map<ArmorType, Part> parts = new EnumMap<>(ArmorType.class);
			for (ArmorType type : ArmorSet.TYPES) {
				Identifier baseId = model(type, "base");
				ItemModel base = new CuboidItemModelWrapper.Unbaked(baseId, Optional.empty(), List.of()).bake(context, transformation);
				ResolvedModel resolved = baker.getModel(baseId);
				TextureSlots slots = resolved.getTopTextureSlots();
				ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
				parts.put(type, new Part(base, properties, transformation));
			}
			return new BlockArmorIconModel(parts);
		}

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

	}

}

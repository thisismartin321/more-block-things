package mcraftyt.moreblockthings.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * Builds the "block armor": per body part, individual faces (planes) onto which a section of the block texture
 * is mapped. Coordinates and texture offsets are taken 1:1 from the original (ModelBAArmor); they apply to a
 * texture normalised to 16x16.
 * <p>
 * The UVs are then converted from the 0..1 range into the rectangle (u0,v0)-(u1,v1). For a loose texture that is
 * (0,0)-(1,1), for a sprite in the block atlas it is the sprite's area.
 */
public class BlockArmorGeometry {

	/** The faces for (waist/feet) live directly in body/legs because their pivot points are identical */
	public static ModelPart buildRoot(ArmorType type, float u0, float v0, float u1, float v1) {
		List<ModelPart.Cube> head = new ArrayList<>(), body = new ArrayList<>(), rightArm = new ArrayList<>(),
				leftArm = new ArrayList<>(), rightLeg = new ArrayList<>(), leftLeg = new ArrayList<>();
		Remap r = new Remap(u0, v0, u1, v1);

		switch (type) {
		case HELMET:
			plane(head, r, 9, 3, -5.0f, -9.0f, -5.0f, 10, 0, 10, false); // top
			plane(head, r, 9, 0, -5.0f, -9.0f, 5.0f, 10, 8, 0, true); // back
			plane(head, r, -1, 8, -3.0f, -1.0f, 5.0f, 6, 1, 0, true); // back bottom
			plane(head, r, 6, -10, -5.0f, -9.0f, -5.0f, 0, 5, 10, false); // right
			plane(head, r, 0, 0, -5.0f, -4.0f, -0.0f, 0, 1, 5, false); // right bottom
			plane(head, r, 6, -10, 5.0f, -9.0f, -5.0f, 0, 5, 10, true); // left
			plane(head, r, 11, 0, 5.0f, -4.0f, -0.0f, 0, 1, 5, true); // left bottom
			plane(head, r, 3, 0, -5.0f, -9.0f, -5.0f, 10, 4, 0, false); // front
			plane(head, r, 0, 4, -5.0F, -5.0F, -5.0F, 1, 1, 0, false); // front left
			plane(head, r, 7, 4, -1.0F, -5.0F, -5.0F, 2, 2, 0, false); // front mid
			plane(head, r, 12, 4, 4.0F, -5.0F, -5.0F, 1, 1, 0, false); // front right
			break;
		case CHESTPLATE:
			plane(body, r, 3, 4, -5.0F, 1.0F, -3.0F, 10, 8, 0, false); // front
			plane(body, r, 4, 12, -4.0F, 9.0F, -3.0F, 8, 1, 0, false); // front bottom mid
			plane(body, r, 5, 13, -3.0F, 10.0F, -3.0F, 6, 1, 0, false); // front bottom
			plane(body, r, 3, 2, -5.0F, -1.0F, -3.0F, 2, 2, 0, false); // front top right 2x2
			plane(body, r, 5, 2, -3.0F, -0.0F, -3.0F, 1, 1, 0, false); // front top right 1x1
			plane(body, r, 11, 2, 3.0F, -1.0F, -3.0F, 2, 2, 0, false); // front top left 2x2
			plane(body, r, 10, 2, 2.0F, -0.0F, -3.0F, 1, 1, 0, false); // front top left 1x1
			plane(body, r, 5, -4, -5.0F, -1.0F, -3.0F, 0, 10, 6, true); // right
			plane(body, r, -1, -4, 5.0F, -1.0F, -3.0F, 0, 10, 6, false); // left
			plane(body, r, 9, 3, -5.0F, 0.0F, 3.0F, 10, 9, 0, true); // back
			plane(body, r, 11, 2, -5.0F, -1.0F, 3.0F, 2, 0, 0, true); // back top right
			plane(body, r, 3, 2, 3.0F, -1.0F, 3.0F, 2, 1, 0, true); // back top left
			plane(body, r, 4, 12, -4.0F, 9.0F, 3.0F, 8, 1, 0, true); // back bottom
			// right arm
			plane(rightArm, r, 5, -4, -4.5F, -3.0F, -3.0F, 0, 6, 6, true); // right
			plane(rightArm, r, 5, -1, 1.5F, -3.0F, -3.0F, 0, 6, 6, true); // left
			plane(rightArm, r, -2, 2, -4.5F, -3.0F, -3.0F, 6, 6, 0, false); // front
			plane(rightArm, r, -8, 2, -4.5F, -3.0F, 3.0F, 6, 6, 0, true); // back
			plane(rightArm, r, 15, 5, -4.5F, -3.0F, -3.0F, 6, 0, 6, false); // top
			// left arm
			plane(leftArm, r, -1, -1, -1.5F, -3.0F, -3.0F, 0, 6, 6, false); // right
			plane(leftArm, r, -1, -4, 4.5F, -3.0F, -3.0F, 0, 6, 6, false); // left
			plane(leftArm, r, 12, 2, -1.5F, -3.0F, -3.0F, 6, 6, 0, false); // front
			plane(leftArm, r, 5, 5, -1.5F, -3.0F, 3.0F, 6, 6, 0, true); // back
			plane(leftArm, r, 15, 5, -1.5F, -3.0F, -3.0F, 6, 0, 6, false); // top
			break;
		case LEGGINGS:
			// waist (a separate part with pivot 0/0/0 in the original, identical to body)
			plane(body, r, 3, 0, -4.5F, 6.5F, -2.5F, 9, 6, 0, false); // front
			plane(body, r, 6, -5, -4.5F, 6.5F, -2.5F, 0, 6, 5, false); // right
			plane(body, r, 6, -5, 4.5F, 6.5F, -2.5F, 0, 6, 5, false); // left
			plane(body, r, 11, 0, -4.5F, 6.5F, 2.5F, 9, 6, 0, true); // back
			// right leg
			plane(rightLeg, r, 6, 0, -2.5F, -0.5F, -2.5F, 0, 10, 5, false); // right
			plane(rightLeg, r, 9, 5, 2.5F, -0.5F, -2.5F, 0, 10, 5, true); // left
			plane(rightLeg, r, 3, 5, -2.5F, -0.5F, -2.5F, 5, 10, 0, false); // front
			plane(rightLeg, r, -1, 5, -2.5F, -0.5F, 2.5F, 5, 10, 0, true); // back
			plane(rightLeg, r, 9, 0, -2.5F, -0.5F, -2.5F, 5, 0, 5, false); // top
			// left leg
			plane(leftLeg, r, 9, 5, -2.5F, -0.5F, -2.5F, 0, 10, 5, false); // right
			plane(leftLeg, r, 6, 0, 2.5F, -0.5F, -2.5F, 0, 10, 5, false); // left
			plane(leftLeg, r, 7, 5, -2.5F, -0.5F, -2.5F, 5, 10, 0, false); // front
			plane(leftLeg, r, 3, 5, -2.5F, -0.5F, 2.5F, 5, 10, 0, true); // back
			plane(leftLeg, r, 9, 0, -2.5F, -0.5F, -2.5F, 5, 0, 5, false); // top
			break;
		case BOOTS:
			// feet (separate parts in the original, with the same pivot as the legs)
			plane(rightLeg, r, 20, 3, -3.0F, 6.0F, -3.0F, 0, 7, 6, false); // right
			plane(rightLeg, r, 21, 3, 3.0F, 6.0F, -3.0F, 0, 7, 6, true); // left
			plane(rightLeg, r, 2, 7, -3.0F, 6.0F, -3.0F, 6, 7, 0, false); // front
			plane(rightLeg, r, -2, 9, -3.0F, 6.0F, 3.0F, 6, 7, 0, true); // back
			plane(rightLeg, r, -1, 5, -3.0F, 13.0F, -3.0F, 6, 0, 6, false); // bottom
			plane(leftLeg, r, 21, 3, -3.0F, 6.0F, -3.0F, 0, 7, 6, true); // right
			plane(leftLeg, r, 0, 3, 3.0F, 6.0F, -3.0F, 0, 7, 6, true); // left
			plane(leftLeg, r, -8, 7, -3.0F, 6.0F, -3.0F, 6, 7, 0, false); // front
			plane(leftLeg, r, 0, 9, -3.0F, 6.0F, 3.0F, 6, 7, 0, true); // back
			plane(leftLeg, r, -1, 5, -3.0F, 13.0F, -3.0F, 6, 0, 6, false); // bottom
			break;
		default:
			break;
		}

		Map<String, ModelPart> children = new HashMap<>();
		// HumanoidModel fetches "hat" via head.getChild("hat"), so it has to be a child of "head"
		Map<String, ModelPart> headChildren = new HashMap<>();
		headChildren.put("hat", part(new ArrayList<>(), 0.0F, 0.0F, 0.0F));
		ModelPart headPart = new ModelPart(head, headChildren);
		headPart.setPos(0.0F, 0.0F, 0.0F);
		headPart.setInitialPose(headPart.storePose());
		children.put("head", headPart);
		children.put("body", part(body, 0.0F, 0.0F, 0.0F));
		children.put("right_arm", part(rightArm, -5.0F, 2.0F, 0.0F));
		children.put("left_arm", part(leftArm, 5.0F, 2.0F, 0.0F));
		children.put("right_leg", part(rightLeg, -1.9F, 12.0F, 0.0F));
		children.put("left_leg", part(leftLeg, 1.9F, 12.0F, 0.0F));
		return new ModelPart(new ArrayList<>(), children);
	}

	private static ModelPart part(List<ModelPart.Cube> cubes, float x, float y, float z) {
		ModelPart part = new ModelPart(cubes, new HashMap<>());
		part.setPos(x, y, z);
		part.setInitialPose(part.storePose());
		return part;
	}

	/** A face (a "cube" with exactly one visible side). The side is determined by the dimension that is 0. */
	private static void plane(List<ModelPart.Cube> cubes, Remap r, int texX, int texY, float x, float y, float z,
			float width, float height, float depth, boolean flip) {
		Direction face;
		if (width == 0) // y-z plane
			face = flip ? Direction.WEST : Direction.EAST;
		else if (height == 0) // x-z plane
			face = flip ? Direction.UP : Direction.DOWN;
		else // x-y plane (depth == 0)
			face = flip ? Direction.SOUTH : Direction.NORTH;

		ModelPart.Cube cube = new ModelPart.Cube(texX, texY, x, y, z, width, height, depth, 0, 0, 0, flip, 16, 16,
				Set.of(face));
		for (ModelPart.Polygon polygon : cube.polygons) {
			ModelPart.Vertex[] vertices = polygon.vertices();
			// The original offsets partly lie outside 0..16 (e.g. 19..29 or -1..5) because the original relied on
			// the loose texture repeating (REPEAT). The atlas has no repetition, so every face is shifted back into
			// the range 0..1 by whole texture lengths.
			float centerU = 0, centerV = 0;
			for (ModelPart.Vertex vertex : vertices) {
				centerU += vertex.u();
				centerV += vertex.v();
			}
			float shiftU = -(float) Math.floor(centerU / vertices.length);
			float shiftV = -(float) Math.floor(centerV / vertices.length);
			for (int i = 0; i < vertices.length; i++) {
				// If a face extends over the edge (e.g. -1..5), the remainder is clamped to the edge
				float u = Math.max(0f, Math.min(1f, vertices[i].u() + shiftU));
				float v = Math.max(0f, Math.min(1f, vertices[i].v() + shiftV));
				vertices[i] = vertices[i].remap(r.u(u), r.v(v));
			}
		}
		cubes.add(cube);
	}

	/** Converts normalised UVs (0..1) into the target rectangle */
	private record Remap(float u0, float v0, float u1, float v1) {
		float u(float u) {
			return u0 + u * (u1 - u0);
		}

		float v(float v) {
			return v0 + v * (v1 - v0);
		}
	}

}

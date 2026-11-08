package mcraftyt.moreblockthings.common.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Replaces the old reflection on BlockBehaviour.Properties with public BlockState methods */
public class BlockUtils {

	/** Hardness; -1 = indestructible */
	public static float getHardness(Block block) {
		try {
			return block.defaultBlockState().getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		} catch (Exception e) {
			return 0.5f;
		}
	}

	public static float getBlastResistance(Block block) {
		try {
			// Location-dependent variant (the parameterless one is deprecated); explosion is not relevant here
			return block.defaultBlockState().getExplosionResistance(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, null);
		} catch (Exception e) {
			return 0f;
		}
	}

	/** Requires the right tool to be mined */
	public static boolean getRequiresTool(Block block) {
		try {
			return block.defaultBlockState().requiresCorrectToolForDrops();
		} catch (Exception e) {
			return true;
		}
	}

	/** ASSUMPTION: replaces Material.isSolid (no longer exists). canOcclude() is false for glass/ice, like the old isSolid */
	public static boolean getIsSolid(Block block) {
		try {
			BlockState state = block.defaultBlockState();
			return state.canOcclude();
		} catch (Exception e) {
			return true;
		}
	}

	/** Light level the block emits (0 = none) */
	public static int getLightLevel(Block block) {
		try {
			return block.defaultBlockState().getLightEmission(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
		} catch (Exception e) {
			return 0;
		}
	}

}

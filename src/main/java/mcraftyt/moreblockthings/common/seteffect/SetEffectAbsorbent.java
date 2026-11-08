package mcraftyt.moreblockthings.common.seteffect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;

/** Sponge armor soaks up water with the key and becomes wet sponge in the process (dries out again after the cooldown) */
public class SetEffectAbsorbent extends SetEffect {

	protected SetEffectAbsorbent() {
		super();
		this.color = YELLOW;
		this.usesButton = true;
	}

	@Override
	public void onTick(ServerPlayer player) {
		super.onTick(player);
		if (!(player.level() instanceof ServerLevel level))
			return;
		if (isOnCooldown(player)) {
			level.sendParticles(ParticleTypes.FALLING_WATER, player.getX(), player.getY() + 1.0d, player.getZ(), 3, 0.2d, 0.5d,
					0.2d, 0);
			return;
		}
		ArmorSet drySet = ArmorSet.getSet(Blocks.SPONGE);
		ArmorSet wetSet = ArmorSet.getSet(Blocks.WET_SPONGE);
		if (drySet == null || wetSet == null)
			return;
		// let wet sponge dry out again
		if (wornSet(player, wetSet))
			swap(player, wetSet, drySet);
		// use dry sponge
		else if (wornSet(player, drySet) && player.mayBuild() && SetEffectEvents.isKeyDown(player)
				&& absorb(level, player.blockPosition())) {
			playSound(level, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F,
					level.getRandom().nextFloat() + 0.5f);
			swap(player, drySet, wetSet);
			setCooldown(player, 60);
			damageArmor(player, 1, false);
		}
	}

	private static boolean wornSet(Player player, ArmorSet set) {
		for (ArmorType type : ArmorSet.TYPES)
			if (player.getItemBySlot(type.getSlot()).getItem() instanceof BlockArmorItem item && item.set == set)
				return true;
		return false;
	}

	/** Replaces all worn pieces of "from" with the same pieces of "to" (durability/components are kept) */
	private static void swap(Player player, ArmorSet from, ArmorSet to) {
		for (ArmorType type : ArmorSet.TYPES) {
			ItemStack old = player.getItemBySlot(type.getSlot());
			if (old.getItem() instanceof BlockArmorItem item && item.set == from && to.getPiece(type) != null)
				player.setItemSlot(type.getSlot(), old.transmuteCopy(to.getPiece(type)));
		}
	}

	/** Like SpongeBlock#removeWaterBreadthFirstSearch, but without a sponge block (position = player) */
	private boolean absorb(Level level, BlockPos startPos) {
		BlockState spongeState = Blocks.SPONGE.defaultBlockState();
		return BlockPos.breadthFirstTraversal(startPos, 6, 65, (pos, consumer) -> {
			for (Direction direction : Direction.values())
				consumer.accept(pos.relative(direction));
		}, pos -> {
			if (pos.equals(startPos))
				return BlockPos.TraversalNodeStatus.ACCEPT;
			BlockState state = level.getBlockState(pos);
			FluidState fluidState = level.getFluidState(pos);
			if (!spongeState.canBeHydrated(level, startPos, fluidState, pos))
				return BlockPos.TraversalNodeStatus.SKIP;
			if (state.getBlock() instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(null, level, pos, state).isEmpty())
				return BlockPos.TraversalNodeStatus.ACCEPT;
			if (state.getBlock() instanceof LiquidBlock) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			} else {
				if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT) && !state.is(Blocks.SEAGRASS)
						&& !state.is(Blocks.TALL_SEAGRASS))
					return BlockPos.TraversalNodeStatus.SKIP;
				BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
				Block.dropResources(state, level, pos, blockEntity);
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			return BlockPos.TraversalNodeStatus.ACCEPT;
		}) > 1;
	}

	@Override
	protected boolean isValid(Block block) {
		return registryNameContains(block, "sponge", "absorb");
	}

}

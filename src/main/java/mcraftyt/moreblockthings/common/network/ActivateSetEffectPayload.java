package mcraftyt.moreblockthings.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.seteffect.SetEffectEvents;

/** Client -> server: set-effect key pressed/released (the player is derived from the connection) */
public record ActivateSetEffectPayload(boolean pressed) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<ActivateSetEffectPayload> TYPE = new CustomPacketPayload.Type<>(
			Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "activate_set_effect"));

	public static final StreamCodec<ByteBuf, ActivateSetEffectPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ActivateSetEffectPayload::pressed, ActivateSetEffectPayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(ActivateSetEffectPayload payload, IPayloadContext context) {
		SetEffectEvents.setKeyDown(context.player().getUUID(), payload.pressed());
	}

}

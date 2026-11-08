package mcraftyt.moreblockthings.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import mcraftyt.moreblockthings.common.MoreBlockThings;
import mcraftyt.moreblockthings.common.network.ActivateSetEffectPayload;

/** Key for activating the set effects (default: R, as in the original). Sends changes to the server. */
public class KeyActivateSetEffect {

	public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
			Identifier.fromNamespaceAndPath(MoreBlockThings.MODID, "main"));

	public static final KeyMapping ACTIVATE_SET_EFFECT = new KeyMapping("key.moreblockthings.activate_set_effect",
			InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);

	private static boolean lastSent = false;

	public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
		event.registerCategory(CATEGORY);
		event.register(ACTIVATE_SET_EFFECT);
	}

	public static void onClientTick(ClientTickEvent.Post event) {
		if (Minecraft.getInstance().player == null) {
			lastSent = false;
			return;
		}
		boolean down = ACTIVATE_SET_EFFECT.isDown();
		if (down != lastSent) {
			lastSent = down;
			ClientPacketDistributor.sendToServer(new ActivateSetEffectPayload(down));
		}
	}

	/** Display name of the key for tooltips */
	public static String getKeyName() {
		return ACTIVATE_SET_EFFECT.getTranslatedKeyMessage().getString().toUpperCase();
	}

}

package mcraftyt.moreblockthings.common;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import mcraftyt.moreblockthings.common.network.ActivateSetEffectPayload;
import mcraftyt.moreblockthings.common.item.ModItems;
import mcraftyt.moreblockthings.common.item.ModRecipes;

@Mod(MoreBlockThings.MODID)
public class MoreBlockThings {

	public static final String MODNAME = "More Block Things";
	public static final String MODID = "moreblockthings";
	public static final Logger LOGGER = LogUtils.getLogger();

	/** Needed to register the dynamically built config only after the sets have been created */
	public static ModContainer container;

	public MoreBlockThings(IEventBus modBus, ModContainer modContainer) {
		container = modContainer;
		// LOWEST: all items of other mods are registered by then, and we see their BlockItems
		modBus.addListener(EventPriority.LOWEST, ModItems::onRegister);
		modBus.addListener(ModItems::onModifyComponents);
		modBus.addListener(MoreBlockThings::registerPayloads);
		ModItems.CREATIVE_TABS.register(modBus);
		ModRecipes.SERIALIZERS.register(modBus);
		if (FMLEnvironment.getDist().isClient()) {
			// Settings in game under Mods -> More Block Things -> Configuration
			modContainer.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
					net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
			modBus.addListener(mcraftyt.moreblockthings.client.BlockArmorClient::onRegisterClientExtensions);
			modBus.addListener(mcraftyt.moreblockthings.client.BlockArmorClient::onModelBakingCompleted);
			modBus.addListener(mcraftyt.moreblockthings.client.BlockArmorClient::onRegisterItemModels);
			modBus.addListener(mcraftyt.moreblockthings.client.KeyActivateSetEffect::onRegisterKeys);
			NeoForge.EVENT_BUS.addListener(mcraftyt.moreblockthings.client.ClientEffectHooks::onMovementInput);
			NeoForge.EVENT_BUS.addListener(mcraftyt.moreblockthings.client.KeyActivateSetEffect::onClientTick);
			NeoForge.EVENT_BUS.addListener(mcraftyt.moreblockthings.client.SetEffectTooltips::onTooltip);
		}
	}


	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("1").playToServer(ActivateSetEffectPayload.TYPE, ActivateSetEffectPayload.STREAM_CODEC,
				ActivateSetEffectPayload::handle);
	}

}

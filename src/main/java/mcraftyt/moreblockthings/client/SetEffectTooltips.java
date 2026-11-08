package mcraftyt.moreblockthings.client;

import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import mcraftyt.moreblockthings.common.item.BlockArmorItem;
import mcraftyt.moreblockthings.common.seteffect.SetEffect;

/** Shows the set effects of an armor piece in the tooltip (name, with key for key-based effects, plus description) */
public class SetEffectTooltips {

	public static void onTooltip(ItemTooltipEvent event) {
		if (!(event.getItemStack().getItem() instanceof BlockArmorItem item))
			return;
		for (SetEffect effect : item.set.setEffects) {
			Component line = effect.getDisplayName().copy().append(Component.literal(": ")
					.withStyle(style -> style.withColor(effect.color)))
					.append(Component.translatable("setEffect." + effect.key() + ".description")
							.withStyle(style -> style.withColor(effect.color)));
			if (effect.usesButton)
				line = line.copy().append(Component.literal(" <" + KeyActivateSetEffect.getKeyName() + ">")
						.withStyle(style -> style.withColor(0x5555FF)));
			event.getToolTip().add(line);
			effect.addTooltip(event.getItemStack(), event.getToolTip());
		}
	}

}

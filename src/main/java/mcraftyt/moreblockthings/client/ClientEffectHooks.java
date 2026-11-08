package mcraftyt.moreblockthings.client;

import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.seteffect.SetEffect;

/** Client hooks for effects that must act exactly between the start of the tick and movement */
public class ClientEffectHooks {

	/** Runs in LocalPlayer#aiStep before movement (travel), i.e. after the tick's water state */
	public static void onMovementInput(MovementInputUpdateEvent event) {
		if (ArmorSet.hasSetEffect(event.getEntity(), SetEffect.ROCKY))
			SetEffect.ROCKY.applyBeforeTravel(event.getEntity());
	}

}

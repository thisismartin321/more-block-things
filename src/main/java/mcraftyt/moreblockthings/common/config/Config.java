package mcraftyt.moreblockthings.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;
import mcraftyt.moreblockthings.common.item.ArmorSet;
import mcraftyt.moreblockthings.common.seteffect.SetEffect;

/**
 * Config as a STARTUP config: the file is read "immediately on registration". This is necessary because the
 * armor values (durability, defense, ...) have to be fixed as data components when the items are created.
 * The downside: STARTUP is not synchronised between server and client (see the NeoForge docs).
 */
public class Config {

	/** How many pieces have to be worn for set effects to be active */
	public static int piecesForSet = 2;
	/** Whether set effects consume durability */
	public static boolean effectsUseDurability = false;
	public static double globalToughnessModifier = 1;
	public static double globalEnchantabilityModifier = 1;
	public static double globalDamageReductionModifier = 1;
	public static double globalKnockbackResistanceModifier = 1;
	public static double globalDurabilityModifier = 1;

	private static class SetOptions {
		ModConfigSpec.BooleanValue enabled;
		ModConfigSpec.IntValue durability;
		ModConfigSpec.DoubleValue damageReduction;
		ModConfigSpec.DoubleValue toughness;
		ModConfigSpec.DoubleValue knockbackResistance;
		ModConfigSpec.IntValue enchantability;
		ModConfigSpec.ConfigValue<List<? extends String>> effects;
	}

	private static final Map<ArmorSet, SetOptions> options = new HashMap<>();
	private static ModConfigSpec.IntValue piecesForSetValue;
	private static ModConfigSpec.BooleanValue effectsUseDurabilityValue;
	private static ModConfigSpec.DoubleValue toughnessValue, enchantabilityValue, damageReductionValue,
			knockbackResistanceValue, durabilityValue;

	/** Builds the spec; must run AFTER {@link ArmorSet#setup()} because options are created for every set */
	public static ModConfigSpec build() {
		ModConfigSpec.Builder b = new ModConfigSpec.Builder();

		b.comment("General settings").push("General");
		piecesForSetValue = b.comment("Specifies how many armor pieces must be worn for a set's effects to work.")
				.defineInRange("Armor pieces required for Set Effects", 2, 1, 4);
		effectsUseDurabilityValue = b.comment("Should Set Effects use durability of worn armor to work")
				.define("Set Effects use durability", false);
		b.pop();

		b.comment("Settings that affect armor stats for all sets").push("Armor Stats");
		durabilityValue = b.comment("Multiplied by armor's durability")
				.defineInRange("Global Durability Modifier", 1.0d, 0d, 999999d);
		damageReductionValue = b.comment("Multiplied by armor's damage reduction")
				.defineInRange("Global Damage Reduction Modifier", 1.0d, 0d, 999999d);
		toughnessValue = b.comment("Multiplied by armor's toughness")
				.defineInRange("Global Tougness Modifier", 1.0d, 0d, 999999d);
		knockbackResistanceValue = b.comment("Multiplied by armor's knockback resistance")
				.defineInRange("Global Knockback Resistance Modifier", 1.0d, 0d, 999999d);
		enchantabilityValue = b.comment("Multiplied by armor's enchantability")
				.defineInRange("Global Enchantability Modifier", 1.0d, 0d, 999999d);
		b.pop();

		b.comment("Configure armor sets").push("Armor_Sets");
		options.clear();
		for (var entry : ArmorSet.modidToSetMap.entrySet()) {
			b.push(clean(entry.getKey()));
			for (ArmorSet set : entry.getValue()) {
				SetOptions o = new SetOptions();
				b.push(clean(set.name));
				o.enabled = b.define("Enabled", true);
				o.durability = b.defineInRange("Armor_Durability", set.durability, 0, 100000);
				o.damageReduction = b.defineInRange("Armor_Damage_Reduction", (double) set.damageReduction, 0d, 1000d);
				o.toughness = b.defineInRange("Armor_Toughness", (double) set.toughness, 0d, 1000d);
				o.knockbackResistance = b.defineInRange("Armor_Knockback_Resistance", (double) set.knockbackResistance, 0d, 1000d);
				o.enchantability = b.defineInRange("Armor_Enchantability", set.enchantability, 0, 1000);
				// Effects of this set; "Name" or "Name (value)", e.g. "Health Boost (6.0)" (default: automatic assignment)
				o.effects = b.comment("Set effects of this armor set. Remove entries to disable them, add names to give the set more effects.")
						.defineList("Set_Effects", set.setEffects.stream().map(SetEffect::writeToString).toList(), () -> "Speedy",
								e -> e instanceof String string && SetEffect.getEffectFromString(string) != null);
				b.pop();
				options.put(set, o);
			}
			b.pop();
		}
		b.pop();

		return b.build();
	}

	/** Dots separate config paths, so do not allow any in the name */
	private static String clean(String s) {
		return s.replace('.', '_').replace('/', '_');
	}

	/** Reads all values into the fields / sets. Afterwards the materials can be created. */
	public static void apply() {
		piecesForSet = piecesForSetValue.get();
		effectsUseDurability = effectsUseDurabilityValue.get();
		globalDurabilityModifier = durabilityValue.get();
		globalDamageReductionModifier = damageReductionValue.get();
		globalToughnessModifier = toughnessValue.get();
		globalKnockbackResistanceModifier = knockbackResistanceValue.get();
		globalEnchantabilityModifier = enchantabilityValue.get();
		for (ArmorSet set : ArmorSet.allSets) {
			SetOptions o = options.get(set);
			set.enabled = o.enabled.get();
			set.durability = o.durability.get();
			set.damageReduction = o.damageReduction.get().floatValue();
			set.toughness = o.toughness.get().floatValue();
			set.knockbackResistance = o.knockbackResistance.get().floatValue();
			set.enchantability = o.enchantability.get();
			// Take effects over from the config (unknown entries are skipped)
			List<SetEffect> effects = new ArrayList<>();
			for (String string : o.effects.get()) {
				SetEffect effect = SetEffect.getEffectFromString(string);
				if (effect != null)
					effects.add(effect);
			}
			set.setEffects = effects;
		}
	}

}

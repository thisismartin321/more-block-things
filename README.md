# More Block Things

A NeoForge 26.2 mod that makes more things out of blocks. Mod ID: `moreblockthings`.

## Modules

### Block Armor

Wear any block as armor. This module is a from-scratch rewrite of the mod
[Block Armor](https://www.curseforge.com/minecraft/mc-mods/block-armor) by **twopiradians** (originally for
Minecraft 1.18.1 on Forge). Credits to twopiradians for the original idea and design.

## Features

- **An armor set for every block.** Sets are generated automatically from all full blocks that are registered in the
  game, including blocks added by other mods. The armor shows the block's own textures on your body.
- **Crafting.** Armor is crafted from the block in the vanilla armor shapes: helmet (5 blocks), chestplate (8),
  leggings (7) and boots (4). All sets are also available in the "More Block Things" creative tab. Pieces can be repaired
  with their block.
- **Armor stats derived from the block.** Defense, toughness and durability are calculated from the block's hardness,
  blast resistance and properties. Every value can be changed in the config, per set and globally.
- **Set effects.** Wearing enough pieces of a set (2 by default) activates its effects, for example:
  - TNT armor explodes when you press the effect key (default: `R`).
  - Obsidian armor increases your maximum health, Slime armor bounces you, Ice armor makes you slippery,
    Netherrack armor sets attackers on fire, and so on.
  - 39 effects in total: Absorbent, Arrow Defence, AutoSmelt, Bonemealer, Crafter, Diving Suit, Ender, Ender Hoarder,
    Experience Giving, Explosive, Falling, Feeder, Fiery, Flame Resistant, Frosty, Health Boost, Hoarder, Illuminated,
    Immovable, Invisibility, Lightweight, Lucky, Musical, Powerful, Prickly, Puller, Pusher, Regrowth, Respawn, Rocky,
    Sleepy, Slimey, Slippery, Slow Motion, Snowy, Soft Fall, Speedy, Time Control and Undying.
  - Tooltips show which effects a piece has and which key activates them.
- **Custom inventory icons and held-item rendering** that are filled with the block's texture, with hand-made textures
  for special blocks (chests, ender chests, cactus, sugar cane, enchanting tables, composters, shulker boxes, beds).
- **Translations** for 22 languages (machine-assisted; corrections from native speakers are welcome).

## Configuration

The config file is `moreblockthings-startup.toml` (in-game: *Mods -> More Block Things -> Config*). It is read at startup,
so changes require a game restart. You can:

- change how many pieces are needed for set effects and whether effects consume durability,
- scale armor durability, damage reduction, toughness, knockback resistance and enchantability globally,
- enable or disable each set, and override its stats,
- edit the `Set_Effects` list of each set to add or remove effects. Entries are an effect name, optionally with a value,
  for example `"Health Boost (6.0)"`.

## Differences from the original

- The developer command of the original is not included.
- **Lucky:** the looting bonus is approximated (mob drops yield about one extra item per stack on average), because
  Minecraft's looting is data-driven now and the old event no longer exists.
- **Illuminated:** a vanilla light block is placed at head height instead of a custom moving light block.
- **Hoarder:** the stored items are kept in the armor pieces' vanilla container component; Hoarder pieces cannot be
  stored inside Hoarder pieces.
- **AutoSmelt** and **Illuminated** are on by default; the key toggles them, but the state is not saved between
  sessions.
- The crafting recipe is a single special recipe that covers all sets, so it does not appear in recipe viewers such as
  JEI. All pieces can be found in the creative tab, which JEI lists.

## Building

Requires Java 25. The Gradle wrapper is set to 9.8.2.

```
./gradlew build
```

The project is based on the official NeoForge 26.2 MDK (NeoForge 26.2.0.90, ModDevGradle 2.0.148). The jar is created
in `build/libs`.

## License

All Rights Reserved. Not affiliated with the original mod or its author.

# GachaCrate

A premium gacha crate plugin for Minecraft Survival servers (Spigot/Paper **1.20.1**, Java 17), with a full in-game GUI editor — no config file diving required.

## Features

- **Full in-game editor** — create crates, add/remove rewards, tweak everything through GUIs and chat prompts
- **Two opening modes** — *Key in Hand* (right-click the key) or *Physical Block* (right-click a placed crate block)
- **Reward types** — physical items and console commands (`{player}` placeholder supported)
- **Rarity system** — fully config-driven rarities with colors, display names and ordering (`COMMON → DIVINE`)
- **Pity system** — guarantee a minimum rarity after N opens
- **Time limits** — seasonal/event crates with start & end dates
- **Animations** — Reel (3-column spin) or Roulette (ring spin), configurable per crate
- **Idle particles** — ambient helix effects above physical crate blocks
- **Holograms** — DecentHolograms integration for crate blocks (optional)
- **Placeholders** — PlaceholderAPI expansion exposing pity progress (`%gachacrate_pity_text_<crate>%`)
- **Broadcasts & fireworks** — announce high-rarity wins server-wide

## Commands

| Command | Description | Permission |
|---|---|---|
| `/gacha` | Opens the admin dashboard GUI | `gacha.admin` |
| `/gacha create <name>` | Creates a new crate | `gacha.admin.create` |
| `/gacha delete <name>` | Deletes a crate permanently | `gacha.admin.delete` |
| `/gacha additem <crate> <rarity> <chance>` | Adds the item in your hand as a reward | `gacha.admin.additem` |
| `/gacha addcmd <crate> <rarity> <chance> <cmd>` | Adds a console command reward | `gacha.admin.addcmd` |
| `/gacha edit <crate>` | Opens the crate editor GUI | `gacha.admin.edit` |
| `/gacha givekey <player> <crate> [amount]` | Gives crate keys | `gacha.admin.givekey` |
| `/gacha view <crate>` | Previews a crate's rewards | `gacha.user.view` |
| `/gacha stats [player]` | Shows crate opening stats | `gacha.user.stats.self` |
| `/gacha setblock <crate>` | Marks the targeted block as a physical crate | `gacha.admin.setblock` |
| `/gacha removeblock` | Removes the physical crate you're looking at | `gacha.admin.setblock` |

## Building

```bash
gradle build
```

The shaded jar lands in `build/libs/GachaCrate-<version>.jar`.

## Data layout

```
plugins/GachaCrate/
├── config.yml        # key appearance, rarities, broadcast settings
├── locations.yml     # physical crate block positions
├── crates/
│   └── <name>.yml    # one file per crate (rewards, pity, time limits…)
└── playerdata/
    └── <uuid>.yml    # per-player opens, pity counters, first-key flag
```

## Integrations (optional)

| Plugin | What it adds |
|---|---|
| [DecentHolograms](https://www.spigotmc.org/resources/decentholograms.96927/) | Floating holograms above crate blocks |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | `%gachacrate_pity_text_<crate>%` placeholder |

## License

MIT © Kazumiii

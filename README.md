# GachaCrate

A gacha crate plugin for Minecraft Survival servers (Spigot / Paper 1.20 – 1.21). Everything can be set up in-game with menus.

## Features

- **In-game editor** (`/gacha`): create crates, add item and command rewards, and set rarities and weights. No file editing needed.
- **Rarities** from `config.yml`, with rewards sorted best-first in the preview.
- **Real chances**: the preview shows each reward's actual % chance.
- **Pity system**: after X unlucky opens in a row, the next open is guaranteed to be a chosen rarity or better.
- **Time-limited crates** with a start and end date.
- **Animations**: Reel, Roulette or Instant. Players can close the menu to skip, and the reward is still given.
- **Opening modes**: right-click a crate block with a key, or right-click anywhere while holding the key.
- **Mass open**: sneak + right-click to open a whole stack of keys at once, with a summary in chat.
- **Physical crates** with idle particle effects and holograms (holograms need [DecentHolograms](https://www.spigotmc.org/resources/96927/)).
- **Announcements** for rare wins, with a firework that doesn't hurt anyone.
- **PlaceholderAPI** placeholders, and PlaceholderAPI placeholders also work inside reward commands.
- **Protection**: crate blocks can't be broken, blown up or pushed by pistons. Keys can't be placed or used for crafting.
- Player data is auto-saved, and an open crate's reward is never lost on logout, closing the menu or a server stop.

## Installing

1. Download the jar from the latest **Actions** run (Build GachaCrate Plugin → Artifacts) or a release.
2. Put it in your server's `plugins` folder and restart.
3. Optional: install DecentHolograms (holograms) and PlaceholderAPI (placeholders).

## Quick start

```
/gacha create vote               # make a crate
/gacha edit vote                 # open its settings, then add rewards
/gacha setblock vote             # look at a chest and turn it into the crate
/gacha givekey Steve vote 5      # give keys
```

## Commands

| Command | What it does | Permission |
|---|---|---|
| `/gacha` | Admin menu (players see help) | `gacha.admin.edit` |
| `/gacha view <crate>` | Preview rewards and chances | `gacha.user.view` |
| `/gacha stats [player]` | Opening stats and pity | `gacha.user.stats.self` / `gacha.admin.stats.other` |
| `/gacha list` | List crates | `gacha.admin.edit` |
| `/gacha edit <crate>` | Edit a crate in the menu | `gacha.admin.edit` |
| `/gacha create <name>` | Create a crate | `gacha.admin.create` |
| `/gacha delete <name>` | Delete a crate (and its blocks) | `gacha.admin.delete` |
| `/gacha additem <crate> <rarity> <weight>` | Add the item in your hand | `gacha.admin.additem` |
| `/gacha addcmd <crate> <rarity> <weight> <command>` | Add a command reward (`{player}` = winner, separate commands with `\|`) | `gacha.admin.addcmd` |
| `/gacha givekey <player\|all> <crate> [amount]` | Give keys | `gacha.admin.givekey` |
| `/gacha open <crate> [player] [amount]` | Open for free (testing / events) | `gacha.admin.open` |
| `/gacha resetpity <player> <crate\|all>` | Reset pity | `gacha.admin.resetpity` |
| `/gacha setblock <crate>` | Make the block you look at a crate | `gacha.admin.setblock` |
| `/gacha removeblock` | Remove the crate you look at | `gacha.admin.setblock` |
| `/gacha reload` | Reload all files | `gacha.admin.reload` |

Aliases: `/gc`, `/crate`.

Every crate also has its own permission, `gacha.user.open.<crate>`, which everyone has by default. Negate it (for example with LuckPerms) to lock a crate to certain ranks.

## Weights and chances

A reward's **weight** is how likely it is compared to the others. A reward with weight 10 is twice as likely as one with weight 5. Its real chance is its weight divided by the total weight of the crate, and the preview shows that %.

## Placeholders (PlaceholderAPI)

| Placeholder | Value |
|---|---|
| `%gachacrate_pity_<crate>%` | Current pity counter |
| `%gachacrate_pity_max_<crate>%` | Pity threshold |
| `%gachacrate_pity_left_<crate>%` | Opens left until guaranteed |
| `%gachacrate_pity_text_<crate>%` | Ready-made line, e.g. "Pity: 12/50 for Mythic" |
| `%gachacrate_opened_<crate>%` | Times opened |
| `%gachacrate_opened_total%` | Total crates opened |
| `%gachacrate_keys_<crate>%` | Keys in inventory |

## Building

```
gradle build
```

The jar ends up in `build/libs/`. GitHub Actions builds it automatically on every push to `main`.

## Upgrading from 1.x

Your `crates.yml`, `locations.yml` and `playerdata/` files keep working. Two changes to know about:

- Crate names used in new crates can only contain letters, numbers, `_` and `-`.
- Command rewards are now saved as a list (`commands:`). The old `command:` line is still read.

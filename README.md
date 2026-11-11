# GachaCrate

A gacha crate plugin for Minecraft Survival servers (Spigot / Paper 1.20 – 1.21). Everything can be set up in-game with menus.

## Features

- **In-game editor** (`/gacha`): create crates, add item and command rewards, and set rarities and weights. No file editing needed.
- **Rarities** from `config.yml`, with rewards sorted best-first in the preview.
- **Real chances**: the preview shows each reward's actual % chance.
- **Pity system**: after X unlucky opens in a row, the next open is guaranteed to be a chosen rarity or better.
- **Time-limited crates** with a start and end date.
- **Animations**:
  - **Reel**: items scroll past and slow down, often stopping just next to the jackpot.
  - **Roulette**: lights spin around the edge while the prize changes.
  - **Card Pick**: pick 1 of 9 face-down cards, then the rest flip to show what you missed.
  - **In-World Show**: the prize rises out of the crate block, spins and bursts into particles, and everyone nearby can watch.
  - **Instant**: no animation.
  Players can close a menu animation to skip it, and the reward is still given.
- **Gacha-style reveals**: the best rarity gets a gold title, the totem animation and a fanfare, and the second best gets its own flashy title.
- **Starter crates**: `/gacha setup` (or the button in the admin menu) creates a ladder of 4 crates: **Vote → Rare → Epic → Legendary**. Each has its own loot, animation and particles, and lower crates can drop keys for the next tier.
- **Hex colors**: use `&#RRGGBB` anywhere colors are allowed (crate names, lore, messages).
- **Update checker**: tells the console and admins when a new version is released.
- **Opening modes**: right-click a crate block with a key, or right-click anywhere while holding the key.
- **Mass open**: sneak + right-click to open a whole stack of keys at once, with a summary in chat.
- **Physical crates** with particle effects in 6 shapes (Helix, Halo, Orbit, Fountain, Pulse, Vortex) and a burst when a crate is opened.
- **Holograms** through your hologram plugin: [DecentHolograms](https://www.spigotmc.org/resources/96927/), [FancyHolograms](https://modrinth.com/plugin/fancyholograms) 2.x, or any other plugin through console commands (`hologram.provider` in config.yml).
- **Clickable green help** (`/gacha help`): click a command to type it, hover to see what it does. Only shows commands the player can use.
- **Big-server friendly**: give keys to offline players (great for webstores like Tebex), an opening cooldown, rank perks through permissions, a win log file for support tickets, background saving, and cached leaderboards.
- **Announcements** for rare wins, with a firework that doesn't hurt anyone.
- **PlaceholderAPI** placeholders, and PlaceholderAPI placeholders also work inside reward commands.
- **Player menu** (`/gacha`): browse crates, claim daily keys, open the shop, and see wins and leaderboards.
- **Daily free keys**: each crate can give players free keys once a day (`/gacha daily`), with a reminder when they join.
- **Key shop**: sell keys for in-game money (`/gacha shop`, `/gacha buy`). Needs [Vault](https://www.spigotmc.org/resources/34315/) and an economy plugin such as EssentialsX.
- **Win history**: players see their last wins, and everyone sees a "Recent Big Wins" feed (`/gacha history`, `/gacha wins`).
- **Leaderboards**: top crate openers overall or per crate (`/gacha top`), also available as placeholders for holograms.
- **Protection**: crate blocks can't be broken, blown up or pushed by pistons. Keys can't be placed or used for crafting.
- Player data is auto-saved, and an open crate's reward is never lost on logout, closing the menu or a server stop.

## Installing

1. Download the jar from the latest **Actions** run (Build GachaCrate Plugin → Artifacts) or a release.
2. Put it in your server's `plugins` folder and restart.
3. Optional: PlaceholderAPI (placeholders), Vault + an economy plugin (key shop), and DecentHolograms or FancyHolograms (holograms).

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
| `/gacha` | Admin menu (players get the player menu) | `gacha.admin.edit` |
| `/gacha menu` | Player menu (what players get from `/gacha`) | — |
| `/gacha view <crate>` | Preview rewards and chances | `gacha.user.view` |
| `/gacha daily` | Claim today's free keys | `gacha.user.daily` |
| `/gacha shop` | Open the key shop | `gacha.user.shop` |
| `/gacha buy <crate> [amount]` | Buy keys | `gacha.user.shop` |
| `/gacha history [player]` | Recent wins | `gacha.user.history` / `gacha.admin.history.other` |
| `/gacha wins` | Server-wide recent big wins | `gacha.user.history` |
| `/gacha top [crate]` | Top crate openers | `gacha.user.top` |
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
| `/gacha setup` | Create the 4 starter crates | `gacha.admin.create` |
| `/gacha reload` | Reload all files | `gacha.admin.reload` |

Aliases: `/gc`, `/crate`.

## Permissions

| Permission | Gives | Default |
|---|---|---|
| `gacha.*` | Everything | op |
| `gacha.admin` | All admin commands | op |
| `gacha.user` | All player features | everyone |
| `gacha.bypass` | All bypasses below | op |
| `gacha.user.open.<crate>` | Open one crate. Negate it to lock a crate to certain ranks. | everyone |
| `gacha.bypass.cooldown` | Ignore `opening.cooldown-seconds` | op |
| `gacha.bypass.timelimit` | Open time-limited crates at any time | op |
| `gacha.daily.multiplier.<n>` | Get n times the daily free keys (e.g. `.2` for VIP) | — |
| `gacha.massopen.<n>` | Open up to n keys at once with sneak + right-click | — |
| `gacha.admin.update` | Hear about new versions on join | op |

Example with LuckPerms, so VIPs get double daily keys and the Legendary crate is VIP-only:
```
/lp group vip permission set gacha.daily.multiplier.2 true
/lp group default permission set gacha.user.open.legendary false
/lp group vip permission set gacha.user.open.legendary true
```

Every command's own permission is listed in `plugin.yml`.

## Offline keys (webstores)

`/gacha givekey <player> <crate> <amount>` works when the player is offline. They get the keys, with a message, the next time they join. You can run it straight from Tebex, CraftingStore or any webstore.

## Daily keys and the shop

Open a crate's settings (`/gacha edit <crate>`):
- **Daily Free Keys**: click to set how many keys each player gets per day (0 = off). The count resets at midnight, server time.
- **Key Price**: click and type a price (0 = not for sale). Players can buy from `/gacha shop`, `/gacha buy`, or the **Buy Keys** button in the crate preview.

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
| `%gachacrate_keys_<crate>%` | Keys of one crate in inventory |
| `%gachacrate_keys_total%` | All keys in inventory |
| `%gachacrate_pending_keys%` | Keys waiting from offline gifts |
| `%gachacrate_daily_ready%` | How many crates have daily keys ready |
| `%gachacrate_last_win%` / `_last_win_rarity%` / `_last_win_crate%` | The player's latest win |
| `%gachacrate_crate_<crate>_name%` | Crate display name (also `_rewards`, `_price`, `_daily`, `_pity`) |
| `%gachacrate_daily_<crate>%` | `Ready` or `Claimed` |
| `%gachacrate_rank%` | The player's leaderboard position |
| `%gachacrate_top_<n>_name%` / `%gachacrate_top_<n>_value%` | Leaderboard, all crates (e.g. `top_1_name`) |
| `%gachacrate_top_<crate>_<n>_name%` / `_value%` | Leaderboard for one crate |

## Building

```
gradle build
```

The jar ends up in `build/libs/`. GitHub Actions builds it automatically on every push to `main`.

## Releasing a new version

1. Change `version` in `build.gradle.kts` (e.g. `2.3.0`) and merge to `main`.
2. On GitHub, go to **Releases → Draft a new release**, create the tag `v2.3.0`, and publish.
3. The **Release GachaCrate** workflow builds the jar and attaches it to the release.

Every server running GachaCrate checks for the latest release, logs a message in the console, and tells admins (`gacha.admin.update`) when they join.

## Upgrading from 1.x

Your `crates.yml`, `locations.yml` and `playerdata/` files keep working. Two changes to know about:

- Crate names used in new crates can only contain letters, numbers, `_` and `-`.
- Command rewards are now saved as a list (`commands:`). The old `command:` line is still read.

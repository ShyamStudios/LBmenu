![example gui](https://cdn.modrinth.com/data/cached_images/7c2dc55280f01c1bae8fb09d2577d7106ca359f0.png)

# 🏆 LBMenu — Leaderboards, Reimagined

[![bStats](https://img.shields.io/bstats/servers/30375.svg)](https://bstats.org/plugin/bukkit/Leaderboard%20Menu/30375)

Transform your **ajLeaderboard / Topper data** into a sleek, fully interactive in-game menu.
No more boring text lists — give your players a **modern, fast, and visually appealing leaderboard experience**.

---

## ✨ Features

* 📊 **Live Leaderboard Data**
  Display real-time stats directly from ajLeaderboards / Topper

* 🖥️ **Beautiful GUI Menus**
  Clean, customizable menus that players actually enjoy using

* ⚡ **High Performance**
  Smart caching system to reduce lag and improve responsiveness

* 🔧 **Fully Configurable**
  Customize titles, layouts, items, lore, and more via simple config

* 🔌 **Seamless Integration**
  Works perfectly with:

  * ajLeaderboards
  * Topper
  * PlaceholderAPI

* 🎯 **Player-Friendly UX**
  Smooth navigation and instant loading menus

---

## 🚀 Why LBMenu?

Most leaderboard plugins show data in **plain chat or holograms**.
LBMenu takes it further by turning that data into a **premium GUI system**:

> 💡 Better visibility
> 💡 Better interaction
> 💡 Better player engagement

---

# Custom Button Actions

Buttons can execute one or more actions when clicked. Add an `actions` section to any supported button configuration.

## Player Command

Executes a command as the player who clicked the button.

```yaml
actions:
  - "[playercommand] leaderboards"
```

### Example

```yaml
back:
  material: RED_STAINED_GLASS_PANE
  name: "&#FF1919Back"
  lore:
    - "&fClick to return"
  slots: 45
  actions:
    - "[playercommand] leaderboards"
```

## Console Command

Executes a command from the server console.

```yaml
actions:
  - "[consolecommand] give %player% diamond 1"
```

### Example

```yaml
reward:
  material: DIAMOND
  name: "&#55FFFFClaim Reward"
  lore:
    - "&fClick to claim your reward"
  slots: 22
  actions:
    - "[consolecommand] give %player% diamond 1"
```

## Message

Sends a message to the player.

```yaml
actions:
  - "[message] &aYou clicked the button!"
```

### Example

```yaml
info:
  material: BOOK
  name: "&#FFD700Information"
  lore:
    - "&fClick to view information"
  slots: 20
  actions:
    - "[message] &aWelcome to the server!"
```

## Close Menu

Closes the player's currently open inventory.

```yaml
actions:
  - "[close]"
```

### Example

```yaml
close:
  material: BARRIER
  name: "&#FF1919Close"
  lore:
    - "&fClick to close this menu"
  slots: 49
  actions:
    - "[close]"
```

## Play Sound

Plays a sound to the player.

```yaml
actions:
  - "[sound] UI_BUTTON_CLICK"
```

### Example

```yaml
sound-test:
  material: NOTE_BLOCK
  name: "&#55FF55Play Sound"
  lore:
    - "&fClick to play a sound"
  slots: 24
  actions:
    - "[sound] UI_BUTTON_CLICK"
```

## Multiple Actions

A button can execute multiple actions. Actions are executed in the same order they are defined.

```yaml
reward:
  material: CHEST
  name: "&#FFD700Claim Reward"
  lore:
    - "&fClick to claim your reward"
  slots: 22
  actions:
    - "[message] &aYou claimed your reward!"
    - "[consolecommand] give %player% diamond 1"
    - "[sound] ENTITY_PLAYER_LEVELUP"
    - "[close]"
```

When the player clicks this button, it will:

1. Send a message to the player.
2. Give the player a diamond using a console command.
3. Play a level-up sound.
4. Close the menu.

## Available Actions

| Action                       | Description                         |
| ---------------------------- | ----------------------------------- |
| `[playercommand] <command>`  | Executes a command as the player    |
| `[consolecommand] <command>` | Executes a command from the console |
| `[message] <message>`        | Sends a message to the player       |
| `[close]`                    | Closes the currently open menu      |
| `[sound] <sound>`            | Plays a sound to the player         |

## Notes

* Do not include `/` before commands.
* Multiple actions are executed in configuration order.
* Invalid or unknown action types are ignored safely.
* Use valid Bukkit/Paper sound names with the `[sound]` action.
* `%player%` can be used where player-name placeholders are supported.

## 🛠️ Commands

```
/lbmenu        - Open the leaderboard menu
/lbmenu reload - Reload configuration
```

---

## 🔑 Permissions

```
lbmenu.open     - اجازه to open the menu
lbmenu.reload   - Reload the plugin
```

---

## ⚙️ Configuration Highlights

* Custom menu layouts
* Configurable refresh & cache timings
* Placeholder-based lore & titles
* Support for multiple leaderboard categories

---

## 📦 Requirements

* Paper / Spigot (1.21+)
* PlaceholderAPI
* ajLeaderboards or Topper

---

## 💬 Support

Need help or found a bug?
Join our support or open an issue we’re happy to help!


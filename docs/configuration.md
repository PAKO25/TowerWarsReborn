# Configuration Guide

This guide describes arena management commands, join‐sign setup, configuration files, permissions, and general setup instructions.

---

## 🏟 Arena Management

### `/towerwars arena list`
- **Description**: View all arenas with their status (enabled/disabled) and whether they are free to join.
- **Permission**: `towerwars.list` _(default: `everyone`)_

### `/towerwars arena configure <arenaName>`
- **Description**:
    - If `<arenaName>` does not exist, creates a new arena.
    - If it exists, opens the custom in‑game arena configurator to edit paths, spawn points, tower spots, lives, etc.
- **Permission**: `towerwars.configure` _(default: `op`)_
- **Check out Arena configuration instructions at the bottom!**

---

## 🔖 Join Signs

1. Place a sign with the **first line** exactly: `[towerwars]`
2. On the **second line**, write the exact name of the arena you want this sign to link to.
3. The plugin will automatically configure the rest.

- **Place‑Sign Permission**: `towerwars.placesign` _(default: `op`)_
- **Use‑Sign Permission**: `towerwars.usesign` _(default: `everyone`)_

---

## 🛠 Configuration Files

All configuration files are located in `plugins/TowerWarsReborn/`.

| File                  | Purpose                                                                                                                                            |
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `config.yml`          | Enables/disables arenas; toggles stat tracking. **Note:** Arenas **not listed** here will **not** be loaded, even if their individual files exist. |
| `mobConfig.yml`       | Balance individual mob types: cost, income granted on send, speed, lives, custom abilities.                                                        |
| `towerConfig.yml`     | Balance tower types: cost, range, splash radius, attack rate, special behaviors.                                                                   |
| `<arenaName>.yml`     | Per‑arena settings (paths, spawns, tower spots, paths). _Do not edit manually._                              |
| `signs.yml`           | Managed join‐sign registry. _Leave alone unless migrating or troubleshooting._                                                                     |

---

## 🔐 Permissions Reference

| Permission             | Default    | Description                                                                   |
|------------------------|------------|-------------------------------------------------------------------------------|
| `towerwars.list`       | `everyone` | View available arenas with their enabled/disabled and free/join status.       |
| `towerwars.configure`  | `op`       | Use `/towerwars arena configure <arenaName>`.                                 |
| `towerwars.placesign`  | `op`       | Place `[towerwars]` arena join signs.                                         |
| `towerwars.usesign`    | `everyone` | Join an arena by clicking a configured join sign.                             |
| `towerwars.play`       | `everyone` | Use `/towerwars join <arenaName>` and `/towerwars leave`.                     |
| `towerwars.forcestart` | `op`       | Start a game before the countdown has finished usign `/towerwars forcestart`. |
| `towerwars.debug`      | `noone`    | `/towerwars debug` and `/towerwars increaseincome <amount>`                   |

---

## 📝 Configuration Instructions

Carefully read these instructions when creating or editing an arena with `/towerwars arena configure <arenaName>`.

### Arena configuration files
These files include mostly raw data that is parsed in the plugin. Modifying it without understanding the plugins inner workings is highly discouraged.

### Arena world
The arena world is the world in which all tracks will be located. You cannot set track spawns, bounds or path waypoints in any other world.
It must be set first since it's the basis for track spawns.
  
### Track spawns
- Each track spawn belongs to a specific track. A track has its own bounds and path waypoints. 
- All tracks are identical in their bounds, path configurations and **ORIENTATION** but can differ elsewhere.
- Track spawns are 'anchors' for each track. They are the only absolute location that is stored, all bounds and path waypoints are vectors and therefore relative to their corresponding track spawns. This allows easier moving and configuring of tracks.
- You need to set at least 2 track spawns and at most 6.

### Track bounds
- Track bounds are 2-dimensional 'corners' of a track. The Y coordinate is ignored, therefore tracks can not be stacked on top of one another. 
- Towers can only be placed INSIDE track bounds.
- The bounds must form a rectangle. 
- They need to be set only for one track spawn and will be automatically configured for all other tracks. The same applies to path waypoints.
  
### Paths
- Paths are made up of waypoints that tell the mobs where to go. 
- Mobs can only move in a straight line parallel to the axes.
- Mobs can also move up or down but only over slabs!
- There can be multiple paths in a single track (but at least one).
  
### Path waypoints
- Waypoints need to be set in the correct order (start to end, marked by white and black smoke particles). 
- Between two neighbouring waypoints at most one coordinate direction can differ.
- Each path must consist of at least 3 waypoints.

### Other
- Tower place material defines the type of block on which towers can be placed.
- Lobby location, if set, teleports players to a "lobby" when they join a queue.
- When enabling the arena some checks are performed to determine configuration validity, but they shouldn't be relied on!
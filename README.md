# Cobblemon Flan Integration

A Fabric server-side mod that integrates Cobblemon with the Flan claim protection system. Protect your claims from unwanted Pokemon activities!

## Features

This mod adds the following Flan permissions for Cobblemon:

| Permission | Description | Default |
|------------|-------------|---------|
| `cobblemon-flan:pokemon_spawn` | Controls wild Pokemon spawning in claims | Allowed (global) |
| `cobblemon-flan:pokemon_catch` | Controls catching Pokemon in claims | Allowed |
| `cobblemon-flan:pokemon_battle` | Controls starting battles in claims | Denied |
| `cobblemon-flan:pokemon_sendout` | Controls sending out Pokemon in claims | Allowed |
| `cobblemon-flan:pokemon_ride` | Controls riding Pokemon in claims | Denied |
| `cobblemon-flan:display_case` | Controls interacting with display cases | Denied |

Claim owners can configure these permissions per-claim through Flan's permission GUI.

## Dependencies

- Minecraft 1.21.1
- Fabric Loader 0.16.5+
- Fabric API
- [Cobblemon](https://modrinth.com/mod/cobblemon) 1.6.0+
- [Flan](https://modrinth.com/mod/flan) 1.12.0+
- [eclipse-core](https://github.com/Cobblemon-Eclipse/eclipse-core) 1.11.0+

## Installation

1. Install all dependencies listed above
2. Place the `cobblemon-flan-x.x.x.jar` in your server's `mods` folder
3. Start the server

## Configuration

Configuration file is located at `config/cobblemon-flan/config.json`:

```json
{
  "protections": {
    "preventWildSpawns": true,
    "preventCatching": true,
    "preventBattles": true,
    "preventSendOut": true,
    "preventRiding": true,
    "preventDisplayCaseInteraction": true,
    "ownerBypass": true
  },
  "messages": {
    "prefix": "<red>[Flan] </red>",
    "cannotCatch": "<yellow>You cannot catch Pokemon in this claim!</yellow>",
    "cannotBattle": "<yellow>You cannot battle Pokemon in this claim!</yellow>",
    "cannotSendOut": "<yellow>You cannot send out Pokemon in this claim!</yellow>",
    "cannotRide": "<yellow>You cannot ride Pokemon in this claim!</yellow>",
    "cannotUseDisplayCase": "<yellow>You cannot interact with display cases in this claim!</yellow>"
  }
}
```

### Protection Settings

| Setting | Description |
|---------|-------------|
| `preventWildSpawns` | Block wild Pokemon from spawning in claims |
| `preventCatching` | Prevent players from catching Pokemon in claims |
| `preventBattles` | Prevent players from starting battles in claims |
| `preventSendOut` | Prevent players from sending out their Pokemon in claims |
| `preventRiding` | Prevent players from riding Pokemon in claims |
| `preventDisplayCaseInteraction` | Prevent players from interacting with display cases (stealing items) |
| `ownerBypass` | Allow claim owners to bypass all restrictions |

## How It Works

When a player attempts a protected action in a claim, the mod checks the Flan permission for that action. If the permission is denied, the action is blocked and the player receives a configurable message.

Permissions can be configured:
- **Per-claim**: Through Flan's claim permission GUI
- **Per-player/group**: Through Flan's player permission system
- **Globally**: Through the mod's config file

## Permissions in Flan GUI

After installing this mod, you'll see new permission options in Flan's claim GUI:

- **Pokemon Spawn** (pig spawn egg icon) - Global permission for wild spawns
- **Pokemon Catch** (ender pearl icon) - Per-player permission for catching
- **Pokemon Battle** (diamond sword icon) - Per-player permission for battles
- **Pokemon Send Out** (egg icon) - Per-player permission for sending out Pokemon
- **Pokemon Ride** (saddle icon) - Per-player permission for riding
- **Display Case** (glass icon) - Per-player permission for interacting with display cases

## Building from Source

```bash
./gradlew build
```

Output JAR will be in `build/libs/`.

## License

MIT License

## Credits

- [Cobblemon](https://cobblemon.com/) - The Pokemon mod for Minecraft
- [Flan](https://github.com/Flemmli97/Flan) - The claim protection mod
- [Eclipse](https://github.com/Cobblemon-Eclipse) - Mod development

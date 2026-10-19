# Project Cradle

A NeoForge Minecraft mod inspired by **Cradle by Will Wight**. Build a sacred artist's foundation, develop a Path, and progress through Gold and the Lord Realm toward Sage, Herald, and Monarch.

The progression follows the books' distinction between power, a sound foundation, personal insight, Icons, and spiritual transformation. **All numerical capacities, cycle counts, costs, recipes, combat effects, and challenge checks below are gameplay balance choices for this mod, not numbers or universal rules stated in the books.** Elixirs and the Spirit Mirror provide playable representations of more involved processes.

## Starting your Path

- New players begin at **Foundation with pure madra**. A core does not need to be unlocked with an item.
- Press **K** to cycle your own madra and grow your capacity. At **1,200 maximum madra**, you advance to Copper.
- The **Foundation Cycling Manual** is an optional tutorial item. Hold right-click for five seconds to complete its tutorial and receive a Tattered Manual if you have never crafted one. The manual reports your current progression requirement.
- Foundation artists cycle their own madra. At Copper, environmental aura becomes available for compatible Paths. **Pure madra has no environmental aura source**; pure artists continue strengthening and recovering their own madra through cycling.
- The Madra Detector helps identify nearby aura. Madra aspect tuning items become usable at Copper.

Core progress is saved with the player and survives death. Older saves keep their achieved ranks and gain the preparation records needed to support those ranks; legacy Unfounded characters now begin at Foundation. The HUD shows current/maximum madra, aspects, rank, Goldsign, soulfire, Icons, training progress, and the next requirement.

## Building a foundation

### Iron

At Copper, reach **1,400 maximum madra**, then use the appropriate Iron body elixir.

| Iron body | Preparation at Copper | Benefit and progression |
| --- | --- | --- |
| Basic | No preparation cycles required | General Iron strength and durability; advancement is limited to Truegold. |
| Perfect | 20 cycling actions | A sound physical foundation for the Lord Realm, with additional armor and movement. The ordinary Iron Body Elixir produces this body. |
| Bloodforged | 20 cycling actions | Perfect foundation, regeneration, and periodic poison cleansing. |
| Raindrop | 20 cycling actions | Perfect foundation, improved movement and attack speed. |
| Steelforged | 20 cycling actions | Perfect foundation, with strength that increases with advancement. |
| Puppeteer | 20 cycling actions | Perfect foundation, with improved control represented by knockback resistance, step height, and safer falls. |

All Iron bodies grant the mod's general Iron health, armor, damage, Resistance, and Strength benefits. The advanced variants are made from an ordinary Iron Body Elixir and additional ingredients. Choose carefully: the body is established when you advance to Iron.

### Jade

At Iron, perform **40 cycling actions** to establish the Jade spiral and reach **1,600 maximum madra**. Both are required for Jade. The spiral improves cycling recovery and establishes the spiritual foundation used for later advancement. Jade unlocks three technique wheel slots.

## The Gold Realm

A Jade with an established spiral can enter Lowgold through three routes:

| Route | How to enter Lowgold | Goldsign and later preparation |
| --- | --- | --- |
| Natural accumulation | Complete 200 cycling actions at Jade and reach 2,000 maximum madra. | No Goldsign is granted by this route. |
| Compatible Remnant | Absorb a Remnant Binding whose aspect matches one of your madra aspects. | Grants an aspect Goldsign. Complete 100 cycling actions after absorption to master the Remnant before entering the Lord Realm. |
| Sacred contract | Use a Sacred Contract on a stronger sacred beast or wild spirit, at least Lowgold. The partner must be available for a bond and compatible with your Path. | Grants a partner-aspect Goldsign. A pure core adopts the partner's madra aspects. |

Continue building capacity to reach **Highgold at 2,400** and **Truegold at 3,600 maximum madra**. Remnant mastery can progress while cycling through the Gold ranks. Natural and contract artists do not need to master an absorbed Remnant.

Goldsigns have physical models and occupy no armor slots. Their original aspect still supplies the existing Goldsign bonus. Repeating Goldsign particles have been removed.

### Goldsign appearances

Automatic appearance follows your current madra types, including compound aspects. These defaults are cosmetic choices for the mod; Paths using similar madra may have different Goldsigns.

| Madra | Automatic appearance | Example |
| --- | --- | --- |
| Sword + Light, or Light | Metal hair, with silver strands and long locks | Jai |
| Sword, Earth, or Pure | Metal forearm bracer with a spirit gem | Cassias |
| Force or Water | Webbed fingers on both hands | Fishers |
| Life, Blood, Shadow, or Hunger | A coiled serpent around the left arm | Sandviper |
| Air | Jade wings that gently move with the player | Naru |
| Fire + Destruction, or Fire, Destruction, or Death | Black eyes with glowing red irises | Blackflame |
| Air + Force, or Dream | Three floating swords and small clouds behind the shoulders | Cloud Hammer |

Compound combinations take priority over single-aspect defaults. Use `/cradle goldsign auto` to follow your madra or choose a Path appearance with `/cradle goldsign jai`, `cassias`, `fisher`, `sandviper`, `naru`, `blackflame`, or `cloud_hammer`. The descriptive style IDs are also accepted and appear in command suggestions. Changing the style requires an existing Goldsign and changes no combat bonuses. Natural Gold artists do not gain a sign through this command.

Hair and eyes follow the head; hands, bracers, and serpents follow their arms; wings and floating swords follow the torso. Classic and slim skins use separate meshes. Arm features also appear when Minecraft renders that arm in first person. Head and back signs are visible in third person and the inventory preview. These are cosmetic wings and swords; they do not grant flight or weapon attacks.

Remnants, sacred beasts, wild spirits, and sacred herbs provide world encounters and progression resources; their Minecraft forms and spawn rules are adaptations.

## Soulspace, soulfire, and the Lord Realm

At **peak Truegold: 5,000 maximum madra**, a Perfect Iron body, an established Jade spiral, and any required Remnant mastery allow you to open your soulspace.

- **Shift + K** opens an eligible soulspace and attempts to weave soulfire while cycling.
- **`/cradle soulspace`** opens the soulspace inventory. It stores **one item**, independently of ordinary inventory and equipment.
- Weaving requires nearby natural aura from **at least three aspects**, each with a sufficient local sample. Water, open sky, stone, plants, and other aura sources can help create a suitable environment. This is the mod's balance check for gathering varied aura.
- Each successful weave grants **0.1 soulfire**. Soulfire Phials add **1 soulfire** after the soulspace is open.

Lord advancement requires a personal revelation as well as capacity and soulfire. Use **`/cradle revelation <personal statement>`** to record the revelation for your current stage. Entering the command without a statement displays your next requirement.

| Advance from | Advance to | Revelation concerns | Maximum madra | Soulfire spent |
| --- | --- | --- | --- | --- |
| Truegold | Underlord | Past: why you began practicing the sacred arts | 5,000 | 1 |
| Underlord | Overlord | Present: who you are now | 7,000 | 5 |
| Overlord | Archlord | Future: who you intend to become | 9,000 | 10 |

Revelations are represented by player-written statements and explicit resource checks. Capacity or a phial alone does not trigger a Lord advancement. A Basic Iron body cannot endure the transformation and remains limited to the Gold Realm.

## Sage, Herald, and Monarch

Sage and Herald are recorded alongside an artist's underlying advancement. They represent different accomplishments.

### Sage and Icons

At **Underlord or higher**, train **100 willpower** through cycling. Select a concept with **`/cradle icon <concept>`**, then use an **Icon Echo** to manifest that Icon and become a Sage.

You can gain **any number of distinct Icons**. Repeat the selection and Echo process for each new concept; **`/cradle icons`** lists all the Icons you have manifested. Selecting an Icon you already possess also selects it for authority use.

Press **K** to express a supported Icon's authority when a suitable target or effect is available, spending **15 madra**. Examples include Strength repelling attackers, Winter slowing them, Life healing the user, and Void dispersing nearby hostile spell projectiles. Custom concepts are recorded; unique authority effects currently cover the implemented concepts. Use **Shift + K** to cycle without invoking authority.

### Herald

Full Herald advancement requires **Archlord**, **12,000 maximum madra**, and **200 willpower**. Use a **Spirit Mirror** to manifest **your own Remnant**, then overcome that personal challenge to complete the fusion. The mod represents the struggle of wills through a timed combat encounter; an ordinary Remnant Binding cannot perform this transformation. Another player's victory over your Remnant does not complete your fusion.

### Monarch

Complete **both Sage and Herald at Archlord** to become a Monarch. A Sage who manifested an Icon earlier must still reach Archlord and satisfy the Herald requirements. Dreadgod encounters and their pressure scale with the world's Monarchs; these systems are gameplay adaptations.

## Gold madra passives

**Lowgold and higher** gain the passives of both current madra aspects. An actual Goldsign is not required: natural Gold artists receive them too. **Monarchs receive every passive**, regardless of their aspects. Use `/cradle passives` or read a Tattered Manual to see your active bonuses.

| Aspect | Additional passive |
| --- | --- |
| Fire | Offensive technique hits ignite targets for four seconds. Projectiles can also ignite a suitable block surface. |
| Water | Breathe underwater without consuming air. |
| Earth | Mine blocks 20% faster, similar to Haste I. |
| Air | Press H to take off and fly in a swimming pose. WASD moves level, Space rises, Shift descends, and holding sprint boosts speed. |
| Pure | No additional passive. |
| Life | Regeneration I while eligible, without replacing stronger regeneration. |
| Death | Add 1 damage to damaging techniques before defenses. |
| Light | Technique hits make targets glow for five seconds. |
| Shadow | Become invisible at light level 7 or lower. Leaving darkness restores ordinary visibility or an existing invisibility potion. |
| Blood | Add 4 maximum health (two hearts). Gaining the bonus does not refill health. |
| Dream | Offensive technique hits cause nausea and prevent that creature attacking the caster briefly. The creature may still attack others; players' attacks against the caster are blocked during the effect too. |
| Destruction | Each spell projectile can break the block it hits. Bedrock, build restrictions, spawn protection, and cancelled break events are respected. |
| Force | Successful hits apply extra knockback. |
| Sword | Add 3 attack damage, equivalent to Strength I, alongside existing Iron bonuses. |
| Hunger | Heal for 20% of actual health damage dealt, capped at 2 health (one heart) per hit. Blocked hits, self damage, and damage over time do not grant healing. |

These are additional passives: individual techniques retain their own aspect effects. Harmless illusions and self/allied buffs do not count as offensive hits. Passive attributes and flight permission update after advancement, aspect changes, login, and respawn; repeated updates do not stack them. Existing creative/spectator flight and other flight grants are preserved. Air flight uses no madra.

### Air flight

At Lowgold with Air madra, or as a Monarch, press **H** to take off directly from the ground. Flight is available while awake, unmounted, outside water and lava, and not gliding with an elytra.

- **WASD** moves horizontally in the direction you face. Looking up or down does not change your altitude.
- **Space** rises and **Shift** descends.
- Hold the **sprint key** (default **Ctrl**) to boost. Release it to return to normal speed.
- Release the movement controls to brake into a hover.
- Press **H** again to stop flying and begin a gentle descent. Slow Falling is renewed by the server until you reach the ground, providing a safe landing. Press H during the descent to resume eligible Air flight.

Vanilla double-jump can also start flight. While flying, Space controls ascent and cannot toggle flight off, even when double-tapped. Use H to stop, or touch the ground to land naturally.

## Techniques and items

The **Project Cradle** creative tab contains progression items, resources, aspect items, and techniques. There are **29 named Path techniques**, each with its own item and registered technique entity:

- **Blackflame:** Dragon's Breath, Burning Cloak, Void Dragon's Dance, Dragon Descends.
- **White Fox:** Fox Fire, Invisible Flame, Phantom Lights, Fox Tail, Fox Dream, Fox Mirror.
- **Sword:** Rippling Sword, Flowing Sword, Endless Sword, Hidden Blade, Final Sword.
- **Venom:** Venom Needles, Venom Blast, Venom Mist.
- **Starlight:** Star Lance, Flowing Starlight, Star's Edge, Serpent's Shadow, Forged Needles.
- **Additional techniques:** Oathsign, Stone Anchor, Cutting Wind, Grasping Sky, Wind Pressure, Forged Wings.

**Basic Striker** spends madra, changes its appearance and effects with the user's aspect, and disperses after five blocks. The general **Forger** creates a brief protective shell for 18 madra; **Enforcer** reinforces the body for 22 madra; **Ruler** requires Jade and commands a nearby damaging aura wave for 26 madra.

Supported madra aspects are Pure, Fire, Water, Air, Earth, Life, Death, Light, Shadow, Blood, Dream, Destruction, Force, Sword, and Hunger. A core stores one or two aspects.

## Controls

All default bindings appear under **Project Cradle** in Minecraft's Controls menu and can be changed there.

| Key | Action |
| --- | --- |
| K | Cycle once, or express an available Sage authority effect. |
| Shift + K | Cycle without authority; at peak Truegold or above, open the soulspace and attempt to weave soulfire. |
| Z, X, C, V, B | Activate independent quick slots 1–5. Techniques cast from unlocked slots 1–4; other activations drop the assigned stack. |
| N | Open the five-slot quick-drop inventory to assign items. These slots are separate from the hotbar and ordinary inventory. |
| R | Open the technique wheel, showing quick slots 1–4. |
| 1–4 inside the wheel | Select the corresponding technique; clicking a slot also selects it. |
| G | Cast the selected technique. |
| H | Toggle Air flight: take off, resume flight, or begin a gentle landing. |
| WASD while flying | Move horizontally using the direction you face. |
| Space / Shift while flying | Rise / descend. |
| Hold sprint while flying (default Ctrl) | Boost flight speed. |

The wheel unlocks one technique slot at Foundation, three at Jade, and four at Truegold. The fifth independent quick slot is a drop slot available through its own binding.

## Commands

Player progression commands:

```text
/cradle revelation
/cradle revelation <personal statement>
/cradle soulspace
/cradle icon <concept>
/cradle icons
/cradle goldsign [style]
/cradle passives
```

Operator commands for configuring madra:

```text
/cradle madra set <player> <current> [maximum]
/cradle madra type <player> <primary> [secondary]
```

Use lowercase aspect IDs such as `fire`, `water`, `pure`, or `destruction`. Changing capacity does not bypass body preparation, the Jade spiral, Remnant mastery, revelations, or spiritual transformation requirements.

## Development and code organization

Use a **Java 25** runtime and the included Gradle wrapper. Standard NeoForge tasks include `runClient` and `build`.

The network protocol is **4**. Use matching mod versions on the client and server.

Gameplay rules live under `net.ok.project_cradle.server`: progression, Iron body benefits, Remnant fusion, contracts, Icon authority, aura sampling, spell effects and costs, item actions, commands, world/player events, and incoming action handlers. These APIs use `ServerPlayer` or `ServerLevel`; the same gameplay logic runs in singleplayer and on a dedicated server. The server validates requests, changes inventories, spends madra, applies effects and damage, and synchronizes results.

The `client` package handles key input, screens, HUD text, renderers, and cosmetic layers. Common packages hold item/entity/menu registration, item-use adapters, saved attachments and codecs, technique definitions, and network packet formats needed on both sides.

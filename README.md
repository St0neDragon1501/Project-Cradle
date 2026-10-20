# Project Cradle

A NeoForge Minecraft mod inspired by **Cradle by Will Wight**. Build a sacred artist's foundation, develop a Path, and progress through Gold and the Lord Realm toward Sage, Herald, and Monarch.

The progression follows the books' distinction between power, a sound foundation, personal insight, Icons, and spiritual transformation. **All numerical capacities, cycle counts, costs, recipes, combat effects, and challenge checks below are gameplay balance choices for this mod, not numbers or universal rules stated in the books.** Elixirs and the Spirit Mirror provide playable representations of more involved processes.

## Starting your Path

- New players begin at **Foundation with pure madra**. A core does not need to be unlocked with an item.
- Hold **K** to keep cycling your own madra and grow your capacity. At **1,200 maximum madra**, you advance to Copper.
- The **Foundation Cycling Manual** is an optional tutorial item. Hold right-click for five seconds to complete its tutorial and receive a Tattered Manual if you have never crafted one. The manual reports your current progression requirement.
- Foundation artists cycle their own madra. At Copper, environmental aura becomes available for compatible Paths. **Pure madra has no environmental aura source**; pure artists continue strengthening and recovering their own madra through cycling.
- The Madra Detector helps identify nearby aura. Madra aspect tuning items become usable at Copper.

Core progress is saved with the player and survives death. Older saves keep their achieved ranks and gain the preparation records needed to support those ranks; legacy Unfounded characters now begin at Foundation. The HUD shows current/maximum madra, aspects, rank, Goldsign, soulfire, Icons, training progress, and the next requirement.

## Cycling and gathering

Holding **K** performs a cycling exercise every half second. At Copper and above, elemental Paths automatically gather from the richest compatible object within four blocks while cycling. Basic internal recovery also works when every nearby source has been depleted. Foundation and Pure artists recover their own madra internally; natural Pure aura is never created. Pleasant outdoor or living surroundings improve their concentration. Sage artists use **Shift + K** to cycle, retaining the existing unmodified K authority command.

Use the **Madra Detector** on a block or creature to gather from that exact source. Use it in the air to choose a nearby compatible source automatically. At Jade, **Shift + use in the air** retains nearby artist sensing. Harvesting refills the core's **existing madra types** and never changes the Path or raises maximum capacity by itself. Cycling remains the training route for growing capacity.

Every harvested block or creature needs **30 seconds of loaded world time** to recharge. The timer is shared by players, so changing players, hands, or tools cannot bypass it. Block timers are saved by position; breaking and replacing the block does not clear them. Creature timers survive loading, portals, and conversions. Gathering from a full core or an incompatible object does not deplete it. The Detector shows the remaining recharge time when a chosen source is exhausted. Sources are not broken or hurt by gathering.

Both the material and the location affect yields. Rich objects provide a bigger burst, a matching biome improves that burst, and nearby sources become unavailable until their own timers finish. Ambient location bonuses are weaker steady recovery; they are not another harvest of a depleted object. Jade spirals improve gathering further. Examples:

| Madra | Example objects | Favorable places or conditions |
| --- | --- | --- |
| Fire | Lit campfires, lava, magma, burning furnaces | Nether, hot sunny deserts |
| Water | Water, ice, snow, wet sponge | Oceans, rivers, rain, snowy biomes |
| Air | Wool, banners, cobwebs, note blocks | Open sky, mountains, storms |
| Earth | Stone, soil, ores, mineral blocks | Caves, mountains, badlands |
| Life | Logs, leaves, crops, flowers, living creatures | Forests, jungles, lush caves |
| Death | Bones, soul sand, skulls, undead creatures | Soul Sand Valley, Deep Dark |
| Light | Lamps, torches, glow blocks, beacons | Daylight under open sky |
| Shadow | Obsidian, tinted glass, dark underground stone | Night, dark caves, Pale Garden |
| Blood | Redstone, nether wart, crimson growths, creatures | Crimson Forest |
| Dream | Amethyst, beds, bookshelves, enchanting tables | Night, flower meadows, warped forests |
| Destruction | TNT, magma, respawn anchors | Basalt Deltas, storms |
| Force | Pistons, anvils, rails, slime blocks | Storms, windy mountains |
| Sword | Metals, anvils, copper, ancient debris | Stony or jagged peaks |
| Hunger | Sculk, soul sand, ender chests, composters | Deep Dark, End |

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
| Air + Force | Three floating swords and small clouds behind the shoulders | Cloud Hammer |
| Dream, including Fire + Dream | A curved ivory fox tail with a white tip and gentle sway | White Fox |

Compound combinations take priority over single-aspect defaults. Use `/cradle goldsign auto` to follow your madra or choose a Path appearance with `/cradle goldsign jai`, `cassias`, `fisher`, `sandviper`, `naru`, `blackflame`, `cloud_hammer`, or `fox_tail`. The descriptive style IDs are also accepted and appear in command suggestions. Changing the style requires an existing Goldsign and changes no combat bonuses. Natural Gold artists do not gain a sign through this command.

Hair and eyes follow the head; hands, bracers, and serpents follow their arms; wings, floating swords, and the fox tail follow the torso. Classic and slim skins use separate meshes. Arm features also appear when Minecraft renders that arm in first person. Head and back signs are visible in third person and the inventory preview. These are cosmetic wings and swords; they do not grant flight or weapon attacks.

Remnants, sacred beasts, wild spirits, and sacred herbs provide world encounters and progression resources. Remnants use a custom spirit model; the other encounters retain their existing Minecraft adaptations.

## Cradle houses

Ten furnished, Asian-architecture-inspired homes can generate in new Overworld terrain. Their complete footprints, including roof overhangs, remain between **5 and 50 blocks on each side**. The smallest is 5×7; the largest is 49×45. The tallest structure is 41 blocks. The collection includes timber cottages, tiled-roof shop houses, a raised bamboo dwelling, Chinese-inspired courtyards, a Korean hanok-inspired garden villa, a Tibetan-inspired mountain lodge, and a Japanese pagoda-inspired residence.

| House | Footprint | Structure ID |
| --- | --- | --- |
| Bamboo Hermitage | 5 × 7 | `bamboo_hermitage` |
| Cherry Tea House | 11 × 13 | `cherry_tea_house` |
| Cedar Minka | 15 × 17 | `cedar_minka` |
| Lantern Shop House | 13 × 19 | `lantern_shop_house` |
| Riverside Stilt House | 19 × 21 | `riverside_stilt_house` |
| Scholar's Courtyard | 25 × 25 | `scholar_courtyard` |
| Moon Gate Villa | 29 × 25 | `moon_gate_villa` |
| Mountain Lodge | 33 × 27 | `mountain_lodge` |
| Jade Clan Manor | 41 × 37 | `jade_clan_manor` |
| Cloud Pagoda Residence | 49 × 45 | `cloud_pagoda_residence` |

The houses use vanilla materials, framed windows, raised entries, tiled or timber roofs, beds, storage, workstations, and supported lighting. Larger homes add gardens, ponds, balconies, and internal stair flights. The pagoda residence has four connected living floors. They are decorative residential interpretations, with Minecraft proportions and materials.

Natural generation selects one design from a shared structure set in plains, forests, taigas, bamboo jungles, cherry groves, and meadows. A placement region is 32×32 chunks, with 12 chunks of separation; terrain and biome suitability can skip a candidate. Houses appear in newly generated terrain. Their templates and data-driven world-generation definitions require no extra client renderer.

With operator permissions, place a house in an existing world:

```mcfunction
/place template project_cradle:houses/cherry_tea_house ~8 ~-1 ~8
```

Replace `cherry_tea_house` with any ID from the table. The template origin is its lowest corner; local Y=0 is the embedded foundation and Y=1 is the main floor for most designs. The stilt house and lodge have raised floors. Optional rotations include `clockwise_90`, `180`, and `counterclockwise_90`. To find a naturally generated example:

```mcfunction
/locate structure project_cradle:jade_clan_manor
```

Templates live in `src/main/resources/data/project_cradle/structure/houses/`. Their reproducible, dependency-free generator is `tools/structures/generate_houses.py`; run it with Python 3 to rebuild the NBT files and `docs/house_catalog.json`.

## Sacred vanilla mobs

Every vanilla mob species can appear as a **sacred variant**, including animals, aquatic life, villagers, monsters, utility mobs, and bosses. New vanilla mob spawns, including natural spawns and world generation, have a **0.1% chance (1 in 1,000)** by default. The common-config setting `sacredBeastNaturalSpawnChance` controls this rarity and supersedes the old `sacredBeastSpawnChance` setting, so an existing config's former 2% default does not keep the old spawn rate. This replaces a small share of ordinary spawns; it does not add another population of mobs. The same selection also covers breeding, spawn eggs, and summoned vanilla mobs. Existing ordinary mobs are not rerolled when their chunks load.

Transforming into a sacred variant now grants a distinct physical appearance. Most grow **15% larger**, with solid features suited to their species and colored by their one or two madra types: horned crowns and dorsal crystals on land beasts, extended crests and feather growths on flying creatures, fins and side scales on aquatic mobs, spiked carapaces on arthropods, crystalline shoulder mantles and coronets on humanoids, and bright cores and crystal growths on cube-like creatures. The Dragon keeps its multipart size and gains its own ridge and horn decorations. Four stable growth variations provide additional differences between individuals. These features supplement the original animated body and equipment; they occupy no equipment slots. Golden names and a few madra-colored motes also identify sacred creatures.

The appearance is chosen on the server, saved, and sent to everyone tracking the creature. It applies to natural sacred spawns, command transformations, century awakenings, and existing sacred beasts after loading. Conversions preserve the same madra colors and growth variation. Sacred variants also gain extra health, attack strength, speed, and knockback resistance. Ordinary species start at Lowgold; powerful creatures have higher ranks, up to Archlord for the Dragon and Wither.

Each variant retains **one or two saved techniques**: Basic Striker, usually joined by a species-themed skill. Ordinary sacred creatures have a 75% chance of a second skill; Underlord and stronger creatures always have one. When a second skill is assigned, it has a one-in-three chance to be a new elemental technique matching one of the creature’s madra aspects, chosen equally between its aspects; otherwise it uses the existing species skill. Examples include Water/Ice Lance or Frost Nova aquatic creatures, a Dream/Light fox with Fox Dream, Dream Snare, or Solar Beam, Fire/Destruction Nether creatures, and Sword/Force iron golems. Existing creatures retain their saved skills. Hostile mobs use their normal targets; peaceful creatures use techniques to defend themselves when attacked. Technique preparation, madra costs, regeneration, cooldowns, and rank scaling run on the server. Tame owners, sacred contract partners, and allies are protected by the technique targeting rules.

On death, a sacred creature leaves **one Remnant carrying its madra and techniques**, including deaths from a Creeper's own explosion. Defeating that Remnant supplies the existing typed binding and one known technique. Conversions retain the original sacred spirit without a new rarity roll. A splitting Slime or Magma Cube leaves its spirit once; its smaller offspring are ordinary creatures. Remnants retain the normal monster restriction in Peaceful difficulty.

Operators can awaken an existing vanilla mob with `/cradle sacred_beast <targets>`. For example, `/cradle sacred_beast @e[type=minecraft:cow,sort=nearest,limit=1]` awakens the nearest cow. Already sacred creatures are skipped.

## Remnants

A **Remnant** is now a registered custom spirit mob (`project_cradle:remnant`), with its own appearance. It floats and phases through obstacles, charges at enemies, and has a translucent body wrapped around a bright madra core. Glowing channels, eyes, wispy ribbons, and a few drifting motes show its spirit nature. Its colors retain one or two madra aspects; stronger ranks make a slightly denser, larger spirit.

- **Humanoid:** a mask-like face, floating torso, spectral hands, and a trailing lower body.
- **Beast:** a horned, muzzled spirit retaining the outline of sacred wildlife.
- **Tree:** a branching spirit with a luminous trunk and crown, available for placing sacred-tree Remnants in worlds.

Developed spirits leave a Remnant on death from **Iron onward**. Sacred beasts also qualify, including beasts from older saves that predate saved ranks. The Remnant inherits its source's rank, aspects, and broad shape; its health and attack strength are a bounded fraction of the living source's strength. Melee contact has short aspect effects such as Fire burns, Water chills, or Force knockback. These are instinctive echoes, not resurrected people or copies of their equipment.

Existing spirits cannot leave another Remnant. Wild spirits, Dreadbeasts, Dreadgods, and entities with a destroyed spirit are excluded. Entities tagged `project_cradle.dreadbeast` or `project_cradle.spirit_destroyed` are explicitly excluded; the latter also supports a persistent boolean of the same name. These markers support future encounters and world configuration. Ordinary flowers, logs, and animals do not automatically become sacred spirits.

Defeating any Remnant drops exactly one **Remnant Binding**, labeled with its source madra (for example, **Remnant Binding: Fire + Dream**), plus **one technique item the Remnant knows**. Both aspects remain stored on a dual binding, and a compatible Jade artist can absorb it using either matching aspect. Different madra bindings remain separate stacks. Technique drops prefer a known technique other than Basic Striker when one is available. Personal Remnants manifested with a **Spirit Mirror** use this custom mob too, with the existing Archlord requirements, owner-linked challenge, and time limit. They still grant fusion through that challenge and cannot be diverted by Fox Dream. They now drop the same binding and one known technique as other Remnants, while retaining zero experience rewards. Previously saved Vex-based Remnants remain compatible with typed binding drops and challenge tracking; their technique drop uses a madra-themed default. New formations use the custom entity.

### Inherited techniques

Every Remnant knows **Basic Striker** and at least one additional technique. Ordinary spirits receive an instinctive technique suited to their primary madra. A player's Remnant inherits a snapshot of all supported technique items carried in their inventory, independent quick slots, and soulspace, together with techniques they have successfully cast. Existing Minecraft item-use statistics recover techniques practiced before explicit history tracking was added. The snapshot is retained when the Remnant is saved; changing the living player's inventory later cannot change it. Existing spirits without saved techniques receive Basic Striker and a madra-themed technique on loading.

Remnants use their known attacks, self-enforcement, protection, and control techniques through server-controlled casting with preparation, cooldowns, and a regenerating madra reservoir. Combat chooses techniques that fit the current target and situation; a technique that has no valid effect is skipped rather than charging madra for a failed cast. Player Counterspell can interrupt a Remnant's preparation. Player-only effects are adapted to mob combat, and charm never controls players or pets.

The **Remnant Spawn Egg** is in the Project Cradle creative tab. Operators can also place spirits with:

```text
/summon project_cradle:remnant
/cradle remnant spawn <primary> [rank] [humanoid|beast|tree] [secondary]
/cradle remnant spawn fire iron beast
/cradle remnant spawn life lowgold tree
/cradle remnant spawn light monarch humanoid air
```

The egg and plain summon default to an Iron spirit with Pure madra. The Cradle command supports all existing aspects and Iron through Monarch; its optional arguments follow the order shown. World spawning, attacks, progression, and drops are controlled by the server. The tree form is available for world building; ordinary tree blocks do not represent a developed sacred plant.

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
| Air | Press H to take off in upright, creative-style flight. WASD moves level, Space rises, Shift descends, and holding sprint boosts speed. |
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

Air flight keeps the player upright, with movement similar to creative flight.

- **WASD** moves horizontally in the direction you face. Looking up or down does not change your altitude.
- **Space** rises and **Shift** descends.
- Hold the **sprint key** (default **Ctrl**) to boost. Release it to return to normal speed.
- Release the movement controls to brake into a hover.
- Press **H** again to stop flying and begin a gentle descent. Slow Falling is renewed by the server until you reach the ground, providing a safe landing. Press H during the descent to resume eligible Air flight.

Vanilla double-jump can also start flight. While flying, Space controls ascent and cannot toggle flight off, even when double-tapped. Use H to stop, or touch the ground to land naturally.

## Techniques and items

The **Project Cradle** creative tab contains progression items, resources, aspect items, and **56 reusable techniques**: Basic Striker, the three general sacred arts, 29 named Path techniques, eight general spells, and 15 elemental techniques. The **29 named Path techniques** each have their own item and registered technique entity:

- **Blackflame:** Dragon's Breath, Burning Cloak, Void Dragon's Dance, Dragon Descends.
- **White Fox:** Fox Fire, Invisible Flame, Phantom Lights, Fox Tail, Fox Dream, Fox Mirror.
- **Sword:** Rippling Sword, Flowing Sword, Endless Sword, Hidden Blade, Final Sword.
- **Venom:** Venom Needles, Venom Blast, Venom Mist.
- **Starlight:** Star Lance, Flowing Starlight, Star's Edge, Serpent's Shadow, Forged Needles.
- **Additional techniques:** Oathsign, Stone Anchor, Cutting Wind, Grasping Sky, Wind Pressure, Forged Wings.

**Basic Striker** spends madra, changes its appearance and effects with the user's aspect, and disperses after a base distance of five blocks, increasing with core refinement and rank. The general **Forger** creates a brief protective shell for 18 base madra; **Enforcer** reinforces the body for 22 base madra; **Ruler** requires Jade and commands a nearby damaging aura wave for 26 base madra.

### Fox Dream charm

**Fox Dream** now prepares an on-hit charm instead of applying blindness, slowness, and nausea in a cone. Cast it normally from your hand or an unlocked technique slot, then attack hostile mobs while the charm window is active. Melee hits and player-owned projectile/technique hits can trigger it.

- Base activation: **22 madra**, a one-second cast, and a five-second cooldown.
- Base charm window: **12 seconds**. Each successful charm lasts up to **10 seconds**.
- The attacked mob must survive, have its AI enabled, and have another hostile it can fight within **8 blocks**. Players, pets, passive creatures, and already-charmed mobs are excluded as targets; bosses and Wardens cannot be charmed.
- Each successful charm spends **4 additional base madra**. No extra payment is taken if you lack madra, no eligible enemy is nearby, or the victim is already charmed.
- The charmed mob fights nearby hostiles and cannot damage players, pets, or passive creatures while charmed. Hearts mark the charm. It ends when its duration expires, no eligible enemy remains nearby, or the caster dies, disconnects, or changes dimension. Unloading the mob also ends its charm; no taming or permanent AI changes are saved.
- Durations, enemy search distance, activation cost, and extra charm cost use the same advancement/refinement scaling as other techniques. Item tooltips show your current durations and costs. Existing Dream Gold passives still apply independently to techniques.

Dream Goldsigns automatically use the new fox tail. Existing Goldsign holders can choose it with `/cradle goldsign fox_tail` (aliases: `fox`, `tail`, `white_fox`); `/cradle goldsign auto` returns to aspect selection. It uses no equipment slot and stays visible with armor.

### Eight general spells

These reusable technique items can be crafted or found in the Project Cradle creative tab. Cast them from your hand or an unlocked technique slot. Their aspect describes the spell itself; your current core still determines your Gold passives.

| Spell | Base madra | Cast time | Cooldown | Effect |
| --- | --- | --- | --- | --- |
| Shield | 30 | Instant | 8 s | Place a physical barrier ahead of you. It blocks projectiles and absorbs attacks until its health runs out or it expires. Recasting replaces your previous shield. |
| Fireball | 28 | 1.2 s | 3 s | Launch a growing orb that explodes on impact, burning nearby foes. Its impact power and blast area grow during travel. |
| Ice Lance | 18 | 0.6 s | 2 s | Fire a fast ice bolt that pierces up to four creatures and slows those it damages. |
| Chain Lightning | 32 | 1 s | 5 s | Strike a visible foe near your crosshair, then chain through up to four nearby foes. Each jump requires a clear line of sight. |
| Blink | 20 | Instant | 4 s | Move along your aim toward a nearby clear space. Collision checks keep your whole body in front of obstructions. |
| Healing Pulse | 25 | 1 s | 7 s | Restore health to yourself and nearby allied players. Healing stops at each target's maximum health. |
| Counterspell | 20 | Instant | 4 s | Cancel hostile projectiles and interrupt enemies preparing techniques in a cone ahead of you. |
| Gravity Well | 35 | 1.2 s | 8 s | Place a five-second vortex at your aim point. It pulls nearby foes inward and deals periodic technique damage. |

Recipes use a book and amethyst shards with ingredients suited to the spell: iron/prismarine for Shield, fire charge/blaze powder for Fireball, snow/prismarine for Ice Lance, copper/blaze rod for Chain Lightning, ender pearls/obsidian for Blink, ghast tear/glistering melon for Healing Pulse, lapis/quartz for Counterspell, and echo shard/obsidian for Gravity Well. Technique items are reusable; casting consumes madra.

### Fifteen elemental techniques

Each madra aspect has an additional reusable technique item, with its own crafting recipe, icon, and effect description. Find them in the **Project Cradle creative tab**, craft them with a book, amethyst shards, and aspect ingredients, or obtain one through an inheriting Remnant's technique drop. They work from your hand and unlocked spell-wheel slots. Their listed aspect describes the technique; your core's current aspects still supply your Gold passives.

| Technique | Aspect | Base madra | Effect |
| --- | --- | --- | --- |
| Flame Ring | Fire | 24 | Burn nearby foes in a ring around the caster. |
| Frost Nova | Water | 26 | Chill and slow nearby foes with a freezing burst. |
| Gale Step | Air | 16 | Dash along your aim through clear space and push nearby foes aside. |
| Stone Aegis | Earth | 28 | Gain a temporary protective shell of absorption and resistance. |
| Spirit Sever | Pure | 24 | Disrupt an aimed foe's technique preparation and weaken them. |
| Bloom Field | Life | 32 | Place a six-second healing field for yourself and nearby allies. |
| Withering Touch | Death | 22 | Inflict wither and damage in a short cone. |
| Solar Beam | Light | 30 | Strike along a bright, aimed ray that stops at walls. |
| Night Veil | Shadow | 20 | Become briefly invisible and move faster. |
| Crimson Siphon | Blood | 26 | Drain a visible foe and recover health from damage actually dealt. |
| Dream Snare | Dream | 24 | Slow and weaken a visible foe, interrupting preparation. |
| Ruin Burst | Destruction | 34 | Detonate a burst of destructive madra at your aim point. |
| Repulsion Wave | Force | 22 | Push nearby foes back with a radial force blast. |
| Blade Tempest | Sword | 32 | Surround yourself with three pulses of cutting madra. |
| Madra Leech | Hunger | 24 | Damage a visible foe and steal a limited amount of their remaining madra. |

The elemental techniques use the same advancement and core-refinement scaling for madra cost, damage, reach, and applicable durations as existing techniques. Persistent fields expire and do not accumulate when recast. Siphons recover only resources actually removed from valid targets. Techniques respect allied targeting, PvP permissions, line of sight, and collision checks. Remnants can inherit, cast, and drop these techniques; new sacred beasts can receive a matching elemental technique as their second skill, passing it to their Remnant on death. All gameplay effects run on the server.

Example operator item command:

```mcfunction
/give @s project_cradle:solar_beam
```

### Casting and progression

Timed techniques display a cast bar above the hotbar and a ring of runes during preparation. You can prepare one technique at a time. **J cancels the current cast**. Taking an enemy hit of at least two hearts, changing or moving the source item, opening a container, sleeping, changing dimensions, or disconnecting also interrupts preparation.

Madra and cooldowns are applied when the technique successfully completes. An interrupted cast spends no madra. A failed effect, such as a blocked Blink, a Healing Pulse with nobody injured, or a Counterspell with nothing to cancel, refunds its payment and does not start its cooldown. Item tooltips show each technique's effect, current madra cost for your level, cast time, and cooldown. Without a player context (such as search indexing), the tooltip labels its cost as a base cost.

Advancement increases supported technique damage, range, duration, and enforcement strength. Growing your core capacity toward the next rank also adds **refinement**, so training within a rank improves your techniques. Sage and Herald accomplishments add power. Individual techniques retain their targeting, collision, and range limits; Basic Striker still disperses after a base distance of five blocks, increasing with core refinement and rank. The table above lists base costs and timings.

Refinement runs from 0–100% between the current rank’s starting capacity and the next rank’s capacity threshold. Each completed rank or full refinement interval adds 18% of base technique damage and madra cost, 8% of base reach, and 5% of base duration. Sage and Herald each add another 10% of base power and madra cost. Costs are rounded to two decimal places, and the same current cost is used for tooltips, preparation checks, payment, and refunds. Enforcer techniques also gain temporary attack damage, movement speed, and armor continuously with this power; repeated casting refreshes the bonus without stacking it. Shield durability and Healing Pulse recovery scale with power. This is a mod balance rule, not a book statistic.

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
| J | Cancel the technique currently being prepared. |
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

The network protocol is **5**. Use matching mod versions on the client and server.

Gameplay rules live under `net.ok.project_cradle.server`: progression, Iron body benefits, Remnant fusion, contracts, Icon authority, aura sampling, spell effects and costs, item actions, commands, world/player events, and incoming action handlers. These APIs use `ServerPlayer` or `ServerLevel`; the same gameplay logic runs in singleplayer and on a dedicated server. The server validates requests, changes inventories, spends madra, applies effects and damage, and synchronizes results.

The `client` package handles key input, screens, HUD text, renderers, and cosmetic layers. Common packages hold item/entity/menu registration, item-use adapters, saved attachments and codecs, technique definitions, and network packet formats needed on both sides.

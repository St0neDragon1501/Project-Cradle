"""The five large, furnished houses in the Cradle house collection.

All geometry is made from ordinary Minecraft blocks.  Coordinates include roof
overhangs; the tallest residence is forty-two blocks above its foundation.
"""

from house_blueprints import Blueprint


def _tile_roof(b, x1, z1, x2, z2, y, tile="deepslate_tiles", rise=4, lifted=True):
    """A solid tiled hip roof, with stair slopes and lifted eave corners."""
    stair = tile.removesuffix("s") + "_stairs"
    # The tile families have plural block names, but singular stair prefixes.
    stair = {
        "deepslate_tiles": "deepslate_tile_stairs",
        "dark_prismarine": "dark_prismarine_stairs",
        "polished_blackstone_bricks": "polished_blackstone_brick_stairs",
        "blackstone": "blackstone_stairs",
    }.get(tile, stair)
    slab = {
        "deepslate_tiles": "deepslate_tile_slab",
        "dark_prismarine": "dark_prismarine_slab",
        "polished_blackstone_bricks": "polished_blackstone_brick_slab",
        "blackstone": "blackstone_slab",
    }.get(tile, tile.removesuffix("s") + "_slab")
    rise = max(1, min(rise, (x2 - x1) // 2, (z2 - z1) // 2))
    for k in range(rise):
        left, right, back, front = x1 + k, x2 - k, z1 + k, z2 - k
        b.box(left, y + k, back, right, y + k, front, tile)
        for x in range(left, right + 1):
            b.set(x, y + k, back, f"{stair}[facing=south,half=bottom,shape=straight,waterlogged=false]")
            b.set(x, y + k, front, f"{stair}[facing=north,half=bottom,shape=straight,waterlogged=false]")
        for z in range(back + 1, front):
            b.set(left, y + k, z, f"{stair}[facing=east,half=bottom,shape=straight,waterlogged=false]")
            b.set(right, y + k, z, f"{stair}[facing=west,half=bottom,shape=straight,waterlogged=false]")
    b.box(x1 + rise, y + rise, z1 + rise, x2 - rise, y + rise, z2 - rise, slab)
    # A narrow ridge finishes the highest plane.  Roofs are never extended out
    # beyond the footprint supplied by the caller.
    if x2 - x1 >= z2 - z1:
        ridge_z = (z1 + z2) // 2
        b.box(x1 + rise, y + rise + 1, ridge_z, x2 - rise, y + rise + 1, ridge_z, slab)
    else:
        ridge_x = (x1 + x2) // 2
        b.box(ridge_x, y + rise + 1, z1 + rise, ridge_x, y + rise + 1, z2 - rise, slab)
    if lifted:
        for x, z, facing in ((x1, z1, "north"), (x2, z1, "north"),
                             (x1, z2, "south"), (x2, z2, "south")):
            b.set(x, y + 1, z, f"{stair}[facing={facing},half=bottom,shape=straight,waterlogged=false]")
            b.set(x, y + 2, z, slab)


def _room(b, x1, z1, x2, z2, *, y=1, h=4, wood="spruce", wall="white_terracotta",
          tile="deepslate_tiles", eaves=1, roof=True, door=True, rise=4):
    b.box(x1, y, z1, x2, y, z2, f"{wood}_planks")
    b.box(x1, y + 1, z1, x2, y + h, z1, wall)
    b.box(x1, y + 1, z2, x2, y + h, z2, wall)
    b.box(x1, y + 1, z1, x1, y + h, z2, wall)
    b.box(x2, y + 1, z1, x2, y + h, z2, wall)
    b.box(x1 + 1, y + 1, z1 + 1, x2 - 1, y + h, z2 - 1, "air")
    for x in (x1, x2):
        for z in (z1, z2):
            b.pillar(x, z, y + 1, y + h, wood=wood)
    b.box(x1, y + h, z1, x2, y + h, z1, f"{wood}_log[axis=x]")
    b.box(x1, y + h, z2, x2, y + h, z2, f"{wood}_log[axis=x]")
    # Flush glass windows leave the complete doorway available for travel.
    for x in range(x1 + 2, x2 - 1, 4):
        if abs(x - (x1 + x2) // 2) > 1:
            for z in (z1, z2):
                b.set(x, y + 2, z, "glass_pane")
                b.set(x, y + 3, z, "glass_pane")
    for z in range(z1 + 2, z2 - 1, 4):
        for x in (x1, x2):
            b.set(x, y + 2, z, "glass_pane")
            b.set(x, y + 3, z, "glass_pane")
    if door:
        b.door((x1 + x2) // 2, y + 1, z2, wood=wood, facing="south")
    if roof:
        _tile_roof(b, x1 - eaves, z1 - eaves, x2 + eaves, z2 + eaves,
                   y + h + 1, tile=tile, rise=rise)


def _path(b, x1, z1, x2, z2, y=1, material="smooth_stone"):
    b.box(x1, y, z1, x2, y, z2, material)


def _bedroom(b, x, z, *, y=2, color="red", wood="spruce"):
    b.bed(x, y, z, color=color, facing="north")
    b.set(x + 2, y, z - 1, "barrel[facing=up,open=false]")
    b.set(x + 2, y, z, f"{wood}_slab[type=bottom,waterlogged=false]")
    b.lantern(x + 2, y + 1, z)


def _table(b, x, z, y=2, wood="spruce"):
    b.set(x, y, z, f"{wood}_fence")
    b.set(x, y + 1, z, f"{wood}_pressure_plate")
    b.set(x - 1, y, z, f"{wood}_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]")
    b.set(x + 1, y, z, f"{wood}_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]")


def _kitchen(b, x, z, y=2):
    b.set(x, y, z, "furnace[facing=south,lit=false]")
    b.set(x + 1, y, z, "crafting_table")
    b.set(x + 2, y, z, "barrel[facing=south,open=false]")
    b.set(x, y + 1, z, "smoker[facing=south,lit=false]")
    b.set(x + 2, y + 1, z, "flower_pot")


def _stone_lantern(b, x, z, y=1):
    b.set(x, y, z, "stone_bricks")
    b.set(x, y + 1, z, "stone_brick_wall")
    b.set(x, y + 2, z, "sea_lantern")
    b.set(x, y + 3, z, "stone_brick_slab")


def _bamboo(b, x, z, height=4):
    b.set(x, 1, z, "grass_block")
    for y in range(2, height + 2):
        leaves = "large" if y >= height else "small"
        b.set(x, y, z, f"bamboo[age=1,leaves={leaves},stage=0]")


def _pond(b, x1, z1, x2, z2):
    b.box(x1 - 1, 1, z1 - 1, x2 + 1, 1, z2 + 1, "mossy_stone_bricks")
    b.box(x1, 0, z1, x2, 0, z2, "clay")
    b.box(x1, 1, z1, x2, 1, z2, "water[level=0]")
    b.set(x1, 2, z1, "lily_pad")


def _stairs(b, x1, x2, zstart, floor, count=8, wood="spruce"):
    """An unobstructed flight ascending north to a full floor landing."""
    b.box(x1, floor + 1, zstart - count + 1, x2, floor + count + 2, zstart, "air")
    for step in range(count):
        z, y = zstart - step, floor + 1 + step
        if y > floor + 1:
            b.box(x1, floor + 1, z, x2, y - 1, z, f"{wood}_planks")
        b.box(x1, y, z, x2, y, z,
              f"{wood}_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")


def _scholar_courtyard():
    b = Blueprint(25, 25)
    b.title = "Scholar's Courtyard"
    b.style = "Chinese scholar courtyard house"
    b.description = "A tiled study, sleeping wing, and kitchen enclose a lotus court with bamboo and covered entry."
    b.platform(0, 0, 24, 24, top=0, material="stone_bricks")
    b.box(1, 1, 1, 23, 1, 23, "gravel")
    _room(b, 4, 3, 20, 9, wood="dark_oak", wall="white_terracotta")
    _room(b, 3, 11, 8, 20, wood="dark_oak", wall="white_terracotta", rise=3)
    _room(b, 16, 11, 21, 20, wood="dark_oak", wall="white_terracotta", rise=3)
    _room(b, 9, 19, 15, 22, wood="dark_oak", wall="white_terracotta", rise=2)
    b.door(12, 2, 19, wood="dark_oak", facing="north")
    b.door(8, 2, 15, wood="dark_oak", facing="east")
    b.door(16, 2, 15, wood="dark_oak", facing="west")
    _path(b, 12, 10, 12, 24)
    _path(b, 9, 15, 15, 15)
    _path(b, 5, 21, 19, 21)
    # The small pool and planting remain to either side of the axial path.
    _pond(b, 9, 12, 10, 13)
    for x, z in ((14, 12), (15, 13), (14, 16), (15, 17)):
        _bamboo(b, x, z, height=3)
    _bedroom(b, 4, 14, color="cyan", wood="dark_oak")
    b.set(4, 2, 18, "loom[facing=east]")
    _kitchen(b, 17, 12)
    _table(b, 18, 18, wood="dark_oak")
    for x in range(5, 20):
        if x not in (11, 12, 13):
            b.set(x, 2, 4, "bookshelf")
    b.set(8, 2, 6, "lectern[facing=south,has_book=false,powered=false]")
    _table(b, 15, 6, wood="dark_oak")
    b.set(5, 2, 7, "cartography_table")
    b.set(19, 2, 7, "barrel[facing=south,open=false]")
    for x, z in ((10, 20), (14, 20), (5, 8), (19, 8)):
        b.lantern(x, 4, z, hanging=True)
    _stone_lantern(b, 2, 23)
    _stone_lantern(b, 22, 23)
    b.set(12, 1, 24, "stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    return b


def _moon_gate_villa():
    b = Blueprint(29, 25)
    b.title = "Moon Gate Villa"
    b.style = "Korean hanok inspired garden residence"
    b.description = "Low dark roofs frame a U-shaped timber home, an open garden, and a quiet pond beside the entry walk."
    b.platform(0, 0, 28, 24, top=0, material="stone_bricks")
    b.box(1, 1, 1, 27, 1, 23, "grass_block")
    _room(b, 5, 3, 23, 9, wood="oak", wall="white_wool", tile="polished_blackstone_bricks", rise=3)
    _room(b, 3, 10, 9, 20, wood="oak", wall="white_wool", tile="polished_blackstone_bricks", rise=3)
    _room(b, 19, 10, 25, 20, wood="oak", wall="white_wool", tile="polished_blackstone_bricks", rise=3)
    b.door(9, 2, 15, wood="oak", facing="east")
    b.door(19, 2, 15, wood="oak", facing="west")
    _path(b, 10, 10, 18, 11, material="oak_planks")
    _path(b, 10, 11, 11, 21, material="oak_planks")
    _path(b, 17, 11, 18, 21, material="oak_planks")
    _path(b, 12, 11, 12, 21, material="smooth_stone")
    _path(b, 12, 21, 14, 24, material="smooth_stone")
    _path(b, 12, 11, 14, 11, material="smooth_stone")
    _pond(b, 14, 14, 15, 16)
    for x, z in ((13, 18), (16, 19), (2, 7), (26, 7)):
        b.set(x, 1, z, "moss_block")
        b.set(x, 2, z, "flowering_azalea")
    for x in (10, 18):
        for z in (11, 19):
            b.pillar(x, z, 2, 4, wood="oak")
            b.lantern(x, 5, z, hanging=True)
    # A tiled residential gate opens onto the stone garden path.
    for x in (11, 17):
        b.pillar(x, 23, 1, 5, wood="oak")
    b.box(11, 5, 23, 17, 5, 23, "stripped_oak_log[axis=x]")
    _tile_roof(b, 10, 22, 18, 24, 6, tile="polished_blackstone_bricks", rise=1)
    _bedroom(b, 4, 13, color="light_blue", wood="oak")
    _bedroom(b, 4, 18, color="white", wood="oak")
    _kitchen(b, 20, 11)
    _table(b, 22, 17, wood="oak")
    _table(b, 14, 6, wood="oak")
    b.set(7, 2, 4, "bookshelf")
    b.set(8, 2, 4, "bookshelf")
    b.set(9, 2, 4, "lectern[facing=south,has_book=false,powered=false]")
    b.set(21, 2, 4, "barrel[facing=south,open=false]")
    b.set(21, 2, 6, "loom[facing=west]")
    for x, z in ((6, 8), (22, 8), (5, 19), (23, 19)):
        b.lantern(x, 4, z, hanging=True)
    _stone_lantern(b, 8, 23)
    _stone_lantern(b, 20, 23)
    for x in (12, 13, 14):
        b.set(x, 1, 24, "stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    return b


def _flat_cap(b, x1, z1, x2, z2, y):
    b.box(x1, y, z1, x2, y, z2, "dark_oak_planks")
    b.box(x1, y + 1, z1, x2, y + 1, z1, "red_terracotta")
    b.box(x1, y + 1, z2, x2, y + 1, z2, "red_terracotta")
    b.box(x1, y + 1, z1, x1, y + 1, z2, "red_terracotta")
    b.box(x2, y + 1, z1, x2, y + 1, z2, "red_terracotta")
    for x in range(x1, x2 + 1):
        for z in (z1, z2):
            b.set(x, y + 2, z, "smooth_stone_slab")
    for z in range(z1 + 1, z2):
        for x in (x1, x2):
            b.set(x, y + 2, z, "smooth_stone_slab")


def _mountain_lodge():
    b = Blueprint(33, 27)
    b.title = "Mountain Lodge"
    b.style = "Tibetan inspired raised stone and timber home"
    b.description = "A whitewashed lodge on a broad stone terrace has a timber balcony, capped roof, and stair to a private upper room."
    b.platform(0, 0, 32, 26, top=0, material="cobblestone")
    b.platform(3, 3, 29, 22, top=3, material="stone_bricks")
    _room(b, 6, 5, 26, 19, y=3, h=5, wood="dark_oak", wall="smooth_sandstone", roof=False)
    # Dark frieze and red cornice distinguish the flat mountain architecture.
    for z in (5, 19):
        b.box(7, 7, z, 25, 7, z, "brown_terracotta")
        b.box(7, 8, z, 25, 8, z, "red_terracotta")
    _flat_cap(b, 5, 4, 27, 20, 9)
    _room(b, 10, 7, 22, 14, y=10, h=4, wood="dark_oak", wall="smooth_sandstone", roof=False)
    _flat_cap(b, 9, 6, 23, 15, 15)
    b.door(10, 11, 10, wood="dark_oak", facing="west")
    _path(b, 7, 7, 9, 17, y=10, material="dark_oak_planks")
    _path(b, 8, 10, 10, 10, y=10, material="dark_oak_planks")
    _stairs(b, 8, 9, 17, 3, count=7, wood="dark_oak")
    # A full landing follows the last stair at z=11.
    _path(b, 8, 10, 10, 10, y=10, material="dark_oak_planks")
    b.box(5, 3, 20, 27, 3, 22, "dark_oak_planks")
    for x in range(5, 28):
        if x not in range(14, 19):
            b.set(x, 4, 22, "dark_oak_fence")
    for x in (5, 27):
        b.box(x, 4, 20, x, 4, 21, "dark_oak_fence")
        b.pillar(x, 20, 4, 7, wood="dark_oak")
        b.lantern(x, 8, 20, hanging=True)
    for step, z in enumerate((26, 25, 24, 23)):
        b.box(14, step, z, 18, step, z,
              "stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    _path(b, 14, 20, 18, 22, y=3, material="dark_oak_planks")
    _bedroom(b, 12, 8, y=4, color="orange", wood="dark_oak")
    _bedroom(b, 21, 9, y=4, color="brown", wood="dark_oak")
    _kitchen(b, 22, 16, y=4)
    _table(b, 17, 15, y=4, wood="dark_oak")
    b.set(13, 4, 16, "loom[facing=east]")
    b.set(13, 4, 17, "barrel[facing=up,open=false]")
    _bedroom(b, 19, 10, y=11, color="red", wood="dark_oak")
    b.set(12, 11, 8, "bookshelf")
    b.set(13, 11, 8, "bookshelf")
    b.set(14, 11, 8, "lectern[facing=south,has_book=false,powered=false]")
    _table(b, 16, 12, y=11, wood="dark_oak")
    for x, z, y in ((16, 6, 7), (7, 18, 7), (25, 18, 7), (11, 13, 13), (21, 13, 13)):
        b.lantern(x, y, z, hanging=True)
    _stone_lantern(b, 11, 24)
    _stone_lantern(b, 21, 24)
    return b


def _jade_clan_manor():
    b = Blueprint(41, 37)
    b.title = "Jade Clan Manor"
    b.style = "Chinese axial courtyard family manor"
    b.description = "A red-pillared family compound with two open courts, jade tile roofs, sleeping wings, kitchen, and formal rear hall."
    b.platform(0, 0, 40, 36, top=0, material="stone_bricks")
    b.box(1, 1, 1, 39, 1, 35, "gravel")
    # The low perimeter makes a domestic compound without closing its entry.
    for x in (1, 39):
        b.box(x, 2, 2, x, 4, 34, "white_terracotta")
        b.box(x, 5, 2, x, 5, 34, "dark_prismarine_slab")
    b.box(1, 2, 1, 39, 4, 1, "white_terracotta")
    b.box(1, 5, 1, 39, 5, 1, "dark_prismarine_slab")
    for x1, z1, x2, z2 in ((8, 3, 32, 10), (3, 12, 11, 28),
                             (29, 12, 37, 28), (14, 18, 26, 22), (13, 30, 27, 34)):
        _room(b, x1, z1, x2, z2, wood="mangrove", wall="white_terracotta", tile="dark_prismarine", rise=3)
        for x in (x1, x2):
            for z in (z1, z2):
                b.box(x, 2, z, x, 5, z, "red_terracotta")
    b.door(20, 2, 18, wood="mangrove", facing="north")
    b.door(20, 2, 30, wood="mangrove", facing="north")
    for z in (16, 25):
        b.door(11, 2, z, wood="mangrove", facing="east")
        b.door(29, 2, z, wood="mangrove", facing="west")
    _path(b, 19, 11, 21, 36, material="polished_andesite")
    _path(b, 12, 16, 28, 16, material="polished_andesite")
    _path(b, 12, 25, 28, 25, material="polished_andesite")
    _pond(b, 24, 12, 26, 14)
    for x, z in ((14, 12), (15, 13), (14, 15), (25, 26), (26, 27)):
        _bamboo(b, x, z, height=3)
    for x1 in (14, 24):
        b.box(x1, 1, 27, x1 + 2, 1, 28, "grass_block")
        b.set(x1, 2, 27, "peony[half=lower]")
        b.set(x1, 3, 27, "peony[half=upper]")
        b.set(x1 + 2, 2, 28, "flowering_azalea")
    _bedroom(b, 4, 15, color="red", wood="mangrove")
    _bedroom(b, 7, 21, color="pink", wood="mangrove")
    _bedroom(b, 4, 26, color="purple", wood="mangrove")
    b.set(9, 2, 13, "loom[facing=west]")
    _kitchen(b, 31, 13)
    _table(b, 33, 19, wood="mangrove")
    _bedroom(b, 33, 26, color="cyan", wood="mangrove")
    b.set(30, 2, 27, "crafting_table")
    for x in (10, 11, 12, 28, 29, 30):
        b.set(x, 2, 4, "bookshelf")
    _table(b, 14, 6, wood="mangrove")
    _table(b, 26, 6, wood="mangrove")
    b.set(20, 2, 4, "lectern[facing=south,has_book=false,powered=false]")
    b.set(9, 2, 8, "barrel[facing=up,open=false]")
    b.set(31, 2, 8, "barrel[facing=up,open=false]")
    _table(b, 16, 20, wood="mangrove")
    _table(b, 24, 20, wood="mangrove")
    b.set(14, 2, 32, "barrel[facing=south,open=false]")
    b.set(26, 2, 32, "barrel[facing=south,open=false]")
    for x, z in ((17, 15), (23, 17), (17, 26), (23, 26)):
        _stone_lantern(b, x, z)
    for x, z in ((10, 9), (30, 9), (4, 27), (36, 27), (15, 33), (25, 33)):
        b.lantern(x, 4, z, hanging=True)
    for x in (19, 20, 21):
        b.set(x, 1, 36, "stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    return b


def _cloud_pagoda_residence():
    b = Blueprint(49, 45)
    b.title = "Cloud Pagoda Residence"
    b.style = "Japanese pagoda inspired multistory family residence"
    b.description = "Four furnished living floors connected by interior stairs rise over a garden, tea room, and bath annex."
    b.platform(0, 0, 48, 44, top=0, material="stone_bricks")
    b.box(1, 1, 1, 47, 1, 43, "grass_block")
    b.platform(9, 8, 39, 34, top=1, material="stone_bricks")
    _room(b, 10, 9, 38, 33, y=1, h=5, wood="dark_oak", wall="white_terracotta", eaves=2, rise=4)
    _room(b, 15, 13, 33, 29, y=9, h=5, wood="dark_oak", wall="white_terracotta", eaves=2, rise=4)
    _room(b, 18, 15, 30, 28, y=17, h=5, wood="dark_oak", wall="white_terracotta", eaves=2, rise=4)
    _room(b, 19, 16, 29, 26, y=25, h=4, wood="dark_oak", wall="white_terracotta", eaves=2, rise=4)
    # Broad balconies and their railings emphasize inhabited upper floors.
    for y, x1, z1, x2, z2 in ((9, 14, 12, 34, 30), (17, 17, 14, 31, 29), (25, 18, 15, 30, 27)):
        for x in range(x1, x2 + 1):
            b.set(x, y, z2, "dark_oak_planks")
            if abs(x - 24) > 1:
                b.set(x, y + 1, z2, "dark_oak_fence")
        for z in range(z1, z2):
            for x in (x1, x2):
                b.set(x, y, z, "dark_oak_planks")
                b.set(x, y + 1, z, "dark_oak_fence")
    # Each flight has a separate footprint, a full landing, and a tall clear
    # stairwell through the tiled roof and next floor.
    _stairs(b, 17, 18, 28, 1, count=8, wood="dark_oak")
    _path(b, 17, 20, 18, 20, y=9, material="dark_oak_planks")
    _stairs(b, 27, 28, 26, 9, count=8, wood="dark_oak")
    _path(b, 27, 18, 28, 18, y=17, material="dark_oak_planks")
    _stairs(b, 21, 22, 25, 17, count=8, wood="dark_oak")
    _path(b, 21, 17, 22, 17, y=25, material="dark_oak_planks")
    _bedroom(b, 12, 14, color="red", wood="dark_oak")
    _bedroom(b, 12, 20, color="white", wood="dark_oak")
    _kitchen(b, 33, 11)
    _table(b, 25, 18, wood="dark_oak")
    _table(b, 30, 26, wood="dark_oak")
    b.set(36, 2, 30, "barrel[facing=up,open=false]")
    b.set(35, 2, 30, "crafting_table")
    _bedroom(b, 21, 16, y=10, color="light_blue", wood="dark_oak")
    _table(b, 23, 24, y=10, wood="dark_oak")
    for x in (17, 18, 19):
        b.set(x, 10, 14, "bookshelf")
    b.set(20, 10, 14, "lectern[facing=south,has_book=false,powered=false]")
    b.set(31, 10, 14, "loom[facing=west]")
    _bedroom(b, 28, 17, y=18, color="purple", wood="dark_oak")
    b.set(24, 18, 16, "cartography_table")
    b.set(25, 18, 16, "bookshelf")
    b.set(19, 18, 27, "barrel[facing=up,open=false]")
    _bedroom(b, 26, 22, y=26, color="white", wood="dark_oak")
    b.set(26, 26, 18, "lectern[facing=south,has_book=false,powered=false]")
    b.set(27, 26, 18, "bookshelf")
    # Finial above the upper roof; even its tip stays below the fifty block cap.
    b.box(24, 35, 21, 24, 38, 21, "dark_oak_fence")
    b.set(24, 39, 21, "gold_block")
    b.set(24, 40, 21, "lightning_rod[facing=up,powered=false,waterlogged=false]")
    _room(b, 3, 34, 9, 40, wood="dark_oak", wall="white_terracotta", rise=3)
    _table(b, 6, 37, wood="dark_oak")
    b.set(4, 2, 35, "barrel[facing=south,open=false]")
    b.set(8, 2, 35, "flower_pot")
    _room(b, 39, 34, 45, 40, wood="dark_oak", wall="white_terracotta", rise=3)
    b.box(40, 1, 35, 42, 1, 37, "smooth_quartz")
    b.box(40, 2, 35, 42, 2, 37, "water[level=0]")
    b.box(40, 2, 35, 42, 2, 35, "smooth_quartz")
    b.box(40, 2, 35, 40, 2, 37, "smooth_quartz")
    b.box(40, 2, 37, 42, 2, 37, "smooth_quartz")
    b.box(42, 2, 35, 42, 2, 37, "smooth_quartz")
    b.set(44, 2, 35, "barrel[facing=south,open=false]")
    b.set(44, 2, 38, "cauldron")
    _path(b, 23, 34, 25, 44, material="polished_andesite")
    _path(b, 6, 41, 42, 41, material="polished_andesite")
    _path(b, 6, 40, 6, 41, material="polished_andesite")
    _path(b, 42, 40, 42, 41, material="polished_andesite")
    _pond(b, 3, 18, 6, 27)
    _path(b, 2, 22, 8, 23, y=2, material="mangrove_planks")
    for z in (21, 24):
        b.box(2, 3, z, 8, 3, z, "mangrove_fence")
    for x, z in ((4, 8), (43, 8), (43, 18), (44, 21), (43, 26)):
        _bamboo(b, x, z, height=5)
    for x, z in ((4, 12), (44, 13), (43, 30), (4, 30)):
        b.set(x, 1, z, "moss_block")
        b.set(x, 2, z, "flowering_azalea")
    for x in (20, 28):
        b.pillar(x, 43, 1, 5, wood="dark_oak")
    b.box(20, 5, 43, 28, 5, 43, "dark_oak_log[axis=x]")
    _tile_roof(b, 19, 42, 29, 44, 6, rise=1)
    for x, z in ((20, 38), (28, 38), (10, 42), (38, 42)):
        _stone_lantern(b, x, z)
    for x, z, y in ((11, 32, 5), (37, 32, 5), (16, 28, 13), (32, 28, 13),
                     (19, 27, 21), (29, 27, 21), (20, 25, 28), (28, 25, 28)):
        b.lantern(x, y, z, hanging=True)
    for x in (23, 24, 25):
        b.set(x, 1, 44, "stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]")
    return b


def build_large() -> list[tuple[str, Blueprint]]:
    return [
        ("scholar_courtyard", _scholar_courtyard()),
        ("moon_gate_villa", _moon_gate_villa()),
        ("mountain_lodge", _mountain_lodge()),
        ("jade_clan_manor", _jade_clan_manor()),
        ("cloud_pagoda_residence", _cloud_pagoda_residence()),
    ]

"""Five compact homes with vanilla blocks and usable furnishings."""
from house_blueprints import Blueprint, state, stairs


def build_small():
    result = []
    b = Blueprint(5, 7)
    b.title = 'Bamboo Hermitage'
    b.style = 'Small East Asian bamboo cottage'
    b.description = 'A single sleeping nook, paper windows, tiled gable and outdoor storage under deep eaves.'
    b.platform(0, 0, 4, 6)
    b.room(1, 1, 3, 5, h=3, wood='bamboo', wall='white_terracotta', roof='dark_oak', roof_type='gable')
    b.bed(2, 2, 2, 'green', 'south')
    b.set(2, 3, 1, 'white_stained_glass')
    b.set(2, 1, 6, stairs('stone_brick', 'north'))
    b.lantern(0, 2, 5)
    b.set(4, 2, 4, state('barrel', facing='up', open=False), {'id':'minecraft:barrel','LootTable':'minecraft:chests/village/village_plains_house'})
    result.append(('bamboo_hermitage', b))

    b = Blueprint(11, 13)
    b.title = 'Cherry Tea House'
    b.style = 'Japanese-inspired cherry cottage'
    b.description = 'Pink timber, a shaded tea veranda, sleeping corner and a stone approach.'
    b.platform(0, 0, 10, 12, material='stone_bricks')
    b.box(1, 1, 1, 9, 1, 11, 'cherry_planks')
    b.room(2, 2, 8, 9, wood='cherry', roof='deepslate_tiles')
    b.bed(3, 2, 4, 'pink', 'north')
    b.set(7, 2, 3, 'crafting_table')
    b.set(7, 2, 4, state('barrel', facing='up', open=False), {'id':'minecraft:barrel','LootTable':'minecraft:chests/village/village_plains_house'})
    b.set(5, 2, 6, state('cherry_slab', type='top', waterlogged=False))
    b.set(4, 2, 6, stairs('cherry', 'east'))
    b.set(6, 2, 6, stairs('cherry', 'west'))
    for x in (1, 9): b.pillar(x, 11, 2, 4, 'cherry')
    b.box(1, 5, 10, 9, 5, 11, state('cherry_slab', type='top', waterlogged=False))
    b.lantern(1, 4, 10, True); b.lantern(9, 4, 10, True)
    for x in (4, 5, 6): b.set(x, 1, 12, stairs('stone_brick', 'north'))
    b.set(1, 2, 1, 'potted_bamboo'); b.set(9, 2, 1, 'potted_cherry_sapling')
    result.append(('cherry_tea_house', b))

    b = Blueprint(15, 17)
    b.title = 'Cedar Minka'
    b.style = 'Japanese minka-inspired timber home'
    b.description = 'A steep gable, exposed beams, twin beds, pantry and a covered hearth.'
    b.platform(1, 1, 13, 15, material='cobblestone')
    b.room(2, 2, 12, 13, wood='spruce', wall='white_terracotta', roof='dark_oak', roof_type='gable', eaves=2)
    b.furnish(3, 3, 11, 11, color='brown')
    b.bed(5, 2, 4, 'brown', 'north')
    b.box(5, 1, 6, 9, 1, 10, 'stone_bricks')
    b.set(7, 2, 8, state('campfire', facing='south', lit=False, signal_fire=False, waterlogged=False))
    b.box(3, 5, 8, 11, 5, 8, state('spruce_log', axis='x'))
    b.lantern(6, 4, 8, True); b.lantern(8, 4, 8, True)
    b.box(5, 1, 14, 9, 1, 16, 'spruce_planks')
    for x in (6, 7, 8): b.set(x, 1, 16, stairs('cobblestone', 'north'))
    for x in (3, 11):
        b.pillar(x, 14, 2, 4)
        b.lantern(x, 5, 14)
    result.append(('cedar_minka', b))

    b = Blueprint(13, 19)
    b.title = 'Lantern Shop House'
    b.style = 'Chinese-inspired two-storey street home'
    b.description = 'A front counter, deep pantry, hanging lanterns and a separate upstairs dwelling.'
    b.platform(1, 1, 11, 18, material='stone_bricks')
    b.room(2, 2, 10, 16, wood='dark_oak', wall='red_terracotta', roof='dark_prismarine', roof_type='flat')
    b.room(3, 3, 9, 14, y=7, h=4, wood='dark_oak', wall='white_terracotta', roof='dark_prismarine')
    b.furnish(4, 4, 8, 12, y=8, color='red')
    for z in (3, 5, 7):
        b.set(9, 2, z, state('barrel', facing='west', open=False), {'id':'minecraft:barrel','LootTable':'minecraft:chests/village/village_plains_house'})
    b.box(6, 2, 9, 9, 2, 9, 'dark_oak_planks')
    b.set(9, 3, 9, 'potted_bamboo')
    b.box(5, 2, 12, 7, 2, 15, 'red_carpet')
    # An actual stair flight and headroom opening connect the two floors.
    for i in range(6):
        x, y, z = 3, 2 + i, 13 - i
        b.set(x, y, z, stairs('dark_oak', 'north'))
        b.box(x, y + 1, z, x, y + 2, z, 'air')
    for x in (3, 9): b.lantern(x, 5, 17, True)
    for x in (5, 6, 7): b.set(x, 1, 18, stairs('stone_brick', 'north'))
    result.append(('lantern_shop_house', b))

    b = Blueprint(19, 21)
    b.title = 'Riverside Stilt House'
    b.style = 'Southeast Asian-inspired bamboo and timber dwelling'
    b.description = 'A raised living floor, shaded fishing deck, long access stair and a small enclosed pond.'
    b.box(1, 1, 1, 17, 3, 16, 'air')
    b.box(1, 4, 1, 17, 4, 16, 'bamboo_mosaic')
    for x in (2, 9, 16):
        for z in (2, 9, 16): b.pillar(x, z, 0, 4, 'dark_oak')
    b.room(4, 3, 14, 12, y=4, h=4, wood='bamboo', wall='dark_oak_planks', roof='dark_prismarine', roof_type='gable', eaves=2)
    b.furnish(5, 4, 13, 10, y=5, color='cyan')
    for x in range(1, 18):
        b.set(x, 5, 1, 'bamboo_fence'); b.set(x, 5, 16, 'bamboo_fence')
    for z in range(1, 17):
        b.set(1, 5, z, 'bamboo_fence'); b.set(17, 5, z, 'bamboo_fence')
    b.box(8, 5, 16, 10, 5, 16, 'air')
    for i in range(4):
        y, z = i + 1, 20 - i
        for x in (8, 9, 10): b.set(x, y, z, stairs('dark_oak', 'north'))
    for x in (3, 15):
        b.pillar(x, 13, 5, 8, 'bamboo')
        b.lantern(x, 9, 13)
    b.platform(1, 18, 5, 20, top=1, material='clay')
    b.box(2, 1, 19, 4, 1, 19, state('water', level=0))
    result.append(('riverside_stilt_house', b))
    return result

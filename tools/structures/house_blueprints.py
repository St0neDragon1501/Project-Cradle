"""Small voxel building helpers for vanilla Minecraft structure templates."""
from __future__ import annotations
import gzip
import re
import struct
from pathlib import Path

DIRECTIONS = {'north': (0, -1), 'south': (0, 1), 'west': (-1, 0), 'east': (1, 0)}

def state(name, **properties):
    name = name.removeprefix('minecraft:')
    return name + ('[' + ','.join(f'{key}={str(value).lower()}' for key, value in sorted(properties.items())) + ']' if properties else '')

def stairs(material, facing='north', half='bottom', shape='straight'):
    return state(material + '_stairs', facing=facing, half=half, shape=shape, waterlogged=False)

ROOF = {
    'deepslate_tiles': ('deepslate_tiles', 'deepslate_tile'),
    'deepslate_bricks': ('deepslate_bricks', 'deepslate_brick'),
    'polished_blackstone_bricks': ('polished_blackstone_bricks', 'polished_blackstone_brick'),
    'stone_bricks': ('stone_bricks', 'stone_brick'),
    'bricks': ('bricks', 'brick'),
    'prismarine': ('prismarine', 'prismarine'),
    'dark_prismarine': ('dark_prismarine', 'dark_prismarine'),
    'mud_bricks': ('mud_bricks', 'mud_brick'),
}

class Blueprint:
    def __init__(self, width, depth):
        self.width, self.depth = width, depth
        self.blocks = {}
        self.title = self.style = self.description = ''

    def set(self, x, y, z, block, nbt=None):
        x, y, z = int(x), int(y), int(z)
        if not (0 <= x < self.width and 0 <= z < self.depth and 0 <= y < 50):
            raise ValueError(f'Block outside {self.width}x{self.depth} footprint or 50-block height: {(x, y, z)}')
        self.blocks[x, y, z] = (block, nbt)

    def box(self, x1, y1, z1, x2, y2, z2, block):
        x1, x2 = sorted((int(x1), int(x2)))
        y1, y2 = sorted((int(y1), int(y2)))
        z1, z2 = sorted((int(z1), int(z2)))
        for y in range(y1, y2 + 1):
            for z in range(z1, z2 + 1):
                for x in range(x1, x2 + 1):
                    self.set(x, y, z, block)

    def platform(self, x1, z1, x2, z2, top=1, material='stone_bricks'):
        self.box(x1, 0, z1, x2, top, z2, material)

    def pillar(self, x, z, y1, y2, wood='spruce'):
        log = 'bamboo_block' if wood == 'bamboo' else wood + '_log'
        self.box(x, y1, z, x, y2, z, state(log, axis='y'))

    def door(self, x, y, z, wood='spruce', facing='south'):
        for dy, half in ((0, 'lower'), (1, 'upper')):
            self.set(x, y + dy, z, state(wood + '_door', facing=facing, half=half, hinge='left', open=False, powered=False))

    def bed(self, x, y, z, color='red', facing='north'):
        dx, dz = DIRECTIONS[facing]
        self.set(x, y, z, state(color + '_bed', facing=facing, part='foot', occupied=False))
        self.set(x + dx, y, z + dz, state(color + '_bed', facing=facing, part='head', occupied=False))

    def lantern(self, x, y, z, hanging=False):
        # Use actual support, including chain drops from a high ceiling.
        if hanging:
            ceiling = next((yy for yy in range(y + 1, min(50, y + 9))
                            if self.blocks.get((x, yy, z), ('air', None))[0] != 'air'), None)
            if ceiling is not None:
                block, nbt = self.blocks[x, ceiling, z]
                if '_slab' in block and ('type=top' in block or '[' not in block):
                    base = block.split('[')[0]
                    self.set(x, ceiling, z, state(base, type='bottom', waterlogged=False), nbt)
                for yy in range(y + 1, ceiling):
                    self.set(x, yy, z, state('iron_chain', axis='y', waterlogged=False))
            else:
                hanging = False
        if not hanging:
            below, nbt = self.blocks.get((x, y - 1, z), ('air', None))
            if below == 'air':
                yy = y - 1
                while yy > 0 and self.blocks.get((x, yy, z), ('air', None))[0] == 'air':
                    self.set(x, yy, z, 'spruce_fence')
                    yy -= 1
            elif '_slab' in below and ('type=bottom' in below or '[' not in below):
                self.set(x, y - 1, z, state(below.split('[')[0], type='top', waterlogged=False), nbt)
        self.set(x, y, z, state('lantern', hanging=hanging, waterlogged=False))

    def roof(self, x1, z1, x2, z2, y, material='deepslate_tiles', kind='hip'):
        full, trim = ROOF.get(material, (material + '_planks', material))
        if kind == 'flat':
            self.box(x1, y, z1, x2, y, z2, full)
            for x in range(x1, x2 + 1):
                self.set(x, y + 1, z1, state(trim + '_slab', type='bottom', waterlogged=False))
                self.set(x, y + 1, z2, state(trim + '_slab', type='bottom', waterlogged=False))
            for z in range(z1, z2 + 1):
                self.set(x1, y + 1, z, state(trim + '_slab', type='bottom', waterlogged=False))
                self.set(x2, y + 1, z, state(trim + '_slab', type='bottom', waterlogged=False))
            return
        half = (min(x2 - x1, z2 - z1) if kind == 'hip' else x2 - x1) // 2
        for inset in range(half + 1):
            left, right = x1 + inset, x2 - inset
            front = z1 + inset if kind == 'hip' else z1
            back = z2 - inset if kind == 'hip' else z2
            if left > right or front > back: break
            self.box(left, y + inset, front, right, y + inset, back, full)
            for z in range(front, back + 1):
                self.set(left, y + inset, z, stairs(trim, 'east'))
                if left != right: self.set(right, y + inset, z, stairs(trim, 'west'))
            if kind == 'hip':
                for x in range(left + 1, right):
                    self.set(x, y + inset, front, stairs(trim, 'south'))
                    if front != back: self.set(x, y + inset, back, stairs(trim, 'north'))
        ridge_y = y + half + 1
        if kind == 'gable':
            center = (x1 + x2) // 2
            for z in range(z1, z2 + 1):
                self.set(center, ridge_y, z, state(trim + '_slab', type='bottom', waterlogged=False))
        else:
            a, b = x1 + half, x2 - half
            c, d = z1 + half, z2 - half
            self.box(a, ridge_y, c, b, ridge_y, d, state(trim + '_slab', type='bottom', waterlogged=False))
        # Raised eave corners and inverted brackets give the stepped roof a curved silhouette.
        for x, z, facing in ((x1, z1, 'south'), (x2, z1, 'south'), (x1, z2, 'north'), (x2, z2, 'north')):
            self.set(x, y, z, stairs(trim, facing, 'top'))
            self.set(x, y + 1, z, state(trim + '_slab', type='bottom', waterlogged=False))

    def room(self, x1, z1, x2, z2, y=1, h=4, wood='spruce', wall='white_terracotta',
             roof='deepslate_tiles', roof_type='hip', eaves=1, door=True, windows=True):
        roof_y = y + h + 1
        clear_height = roof_y + min(x2 - x1 + 2 * eaves, z2 - z1 + 2 * eaves) // 2 + 2
        self.box(x1, y + 1, z1, x2, min(49, clear_height), z2, 'air')
        self.box(x1, y, z1, x2, y, z2, wood + '_planks')
        for yy in range(y + 1, y + h + 1):
            for x in range(x1, x2 + 1):
                self.set(x, yy, z1, wall); self.set(x, yy, z2, wall)
            for z in range(z1, z2 + 1):
                self.set(x1, yy, z, wall); self.set(x2, yy, z, wall)
        for x in (x1, x2):
            for z in (z1, z2): self.pillar(x, z, y + 1, roof_y - 1, wood)
        beam = 'bamboo_block' if wood == 'bamboo' else wood + '_log'
        self.box(x1, y + h, z1, x2, y + h, z1, state(beam, axis='x'))
        self.box(x1, y + h, z2, x2, y + h, z2, state(beam, axis='x'))
        self.box(x1, y + h, z1, x1, y + h, z2, state(beam, axis='z'))
        self.box(x2, y + h, z1, x2, y + h, z2, state(beam, axis='z'))
        if windows:
            for x in range(x1 + 2, x2, 3):
                self.set(x, y + 2, z1, 'white_stained_glass')
                self.set(x, y + 2, z2, 'white_stained_glass')
            for z in range(z1 + 2, z2, 3):
                self.set(x1, y + 2, z, 'white_stained_glass')
                self.set(x2, y + 2, z, 'white_stained_glass')
        if door: self.door((x1 + x2) // 2, y + 1, z2, wood)
        self.roof(x1 - eaves, z1 - eaves, x2 + eaves, z2 + eaves, roof_y, roof, roof_type)

    def furnish(self, x1, z1, x2, z2, y=2, color='red'):
        self.bed(x1, y, z1 + 1, color, 'north')
        self.set(x2, y, z1, 'crafting_table')
        self.set(x2, y, z1 + 1, state('furnace', facing='west', lit=False))
        self.set(x2, y, z2, state('barrel', facing='up', open=False),
                 {'id':'minecraft:barrel', 'LootTable':'minecraft:chests/village/village_plains_house'})
        self.set(x1, y, z2, 'bookshelf')
        self.lantern(x1, y + 1, z2)

    @property
    def height(self): return max((pos[1] for pos in self.blocks), default=4) + 1

    def write(self, path):
        palette, lookup, blocks = [], {}, []
        for pos, (block, nbt) in sorted(self.blocks.items(), key=lambda item: (item[0][1], item[0][2], item[0][0])):
            base = block.split('[')[0].removeprefix('minecraft:')
            if base == 'glass_pane':
                block = 'white_stained_glass'
            elif base.endswith('_fence'):
                x, y, z = pos
                connections = {}
                for direction, (dx, dz) in DIRECTIONS.items():
                    neighbor = self.blocks.get((x + dx, y, z + dz), ('air', None))[0]
                    neighbor = neighbor.split('[')[0].removeprefix('minecraft:')
                    partial = ('_slab', '_stairs', '_door', '_trapdoor', '_bed', '_pressure_plate', '_carpet')
                    non_solid = neighbor in ('air', 'water', 'lava', 'lantern', 'iron_chain', 'bamboo', 'lily_pad', 'flower_pot', 'lightning_rod')
                    non_solid |= neighbor.startswith('potted_') or neighbor.endswith(partial)
                    connections[direction] = neighbor.endswith('_fence') or not non_solid
                block = state(base, **connections, waterlogged=False)
            block = block if ':' in block else 'minecraft:' + block
            if block not in lookup:
                match = re.fullmatch(r'([^\[]+)(?:\[(.*)\])?', block)
                entry = {'Name': match.group(1)}
                if match.group(2): entry['Properties'] = dict(pair.split('=', 1) for pair in match.group(2).split(','))
                lookup[block] = len(palette); palette.append(entry)
            entry = {'pos': list(pos), 'state': lookup[block]}
            if nbt is not None: entry['nbt'] = nbt
            blocks.append(entry)
        data = {'DataVersion':4903, 'size':[self.width, self.height, self.depth], 'palette':palette, 'blocks':blocks, 'entities':[]}
        path = Path(path); path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(gzip.compress(bytes([10]) + utf('') + payload(10, data), mtime=0))
        return {'width':self.width, 'depth':self.depth, 'height':self.height, 'placed_blocks':sum(block != 'air' for block, _ in self.blocks.values()), 'palette':len(palette)}

def utf(value):
    encoded = value.encode('utf-8')
    return struct.pack('>H', len(encoded)) + encoded

def kind(value):
    if isinstance(value, dict): return 10
    if isinstance(value, list): return 9
    if isinstance(value, str): return 8
    if isinstance(value, bool): return 1
    if isinstance(value, int): return 3
    if isinstance(value, float): return 6
    raise TypeError(type(value))

def payload(tag, value):
    if tag == 10:
        return b''.join(bytes([kind(v)]) + utf(k) + payload(kind(v), v) for k, v in value.items()) + bytes([0])
    if tag == 9:
        child = kind(value[0]) if value else 0
        return bytes([child]) + struct.pack('>i', len(value)) + b''.join(payload(child, v) for v in value)
    if tag == 8: return utf(value)
    if tag == 3: return struct.pack('>i', value)
    if tag == 1: return struct.pack('>b', int(value))
    if tag == 6: return struct.pack('>d', value)
    raise TypeError(tag)

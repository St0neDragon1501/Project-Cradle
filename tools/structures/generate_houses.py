"""Generate ten standalone vanilla structure NBT files. No third-party packages needed."""
from pathlib import Path
import argparse
import json
from small_houses import build_small
from large_houses import build_large


def main():
    root = Path(__file__).resolve().parents[2]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=root / 'src/main/resources/data/project_cradle/structure/houses')
    parser.add_argument('--catalog', type=Path, default=root / 'docs/house_catalog.json')
    args = parser.parse_args()
    catalog = []
    for identifier, blueprint in build_small() + build_large():
        details = blueprint.write(args.output / (identifier + '.nbt'))
        catalog.append(dict(id=identifier, name=blueprint.title, style=blueprint.style, description=blueprint.description, **details))
        print(f'{identifier}: {details["width"]} x {details["depth"]}, height {details["height"]}')
    args.catalog.parent.mkdir(parents=True, exist_ok=True)
    args.catalog.write_text(json.dumps(catalog, indent=2) + '\n')
    print(f'Generated {len(catalog)} house templates in {args.output}')

if __name__ == '__main__':
    main()

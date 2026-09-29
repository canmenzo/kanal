"""Build meta.json, the country/category map kanal attaches to M3U channels.

Reads the iptv-org channel database and writes a flat JSON object from
tvg-id to "CC|category1,category2". Host the output next to channels.json
and point META_URL at it.

    python3 tools/make_meta.py            # writes ./meta.json
    python3 tools/make_meta.py out.json
"""

import json
import sys
import urllib.request

SOURCE = "https://iptv-org.github.io/api/channels.json"


def main() -> None:
    out = sys.argv[1] if len(sys.argv) > 1 else "meta.json"
    with urllib.request.urlopen(SOURCE, timeout=60) as resp:
        channels = json.load(resp)
    meta = {
        c["id"]: f"{c.get('country') or ''}|{','.join(c.get('categories') or [])}"
        for c in sorted(channels, key=lambda c: c["id"])
    }
    with open(out, "w", encoding="utf-8") as f:
        json.dump(meta, f, ensure_ascii=False, separators=(",", ":"))
    print(f"Wrote {len(meta):,} channels to {out}")


if __name__ == "__main__":
    main()

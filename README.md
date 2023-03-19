# Intent Graph

## How to add intent to your mod

1. Create file at `intentgraph/intents/intents.json` in your resource folder.
   Make sure it's in that path of your `.jar` file after packaging.
2. Add your intent graph info in that file.
3. If needed, create `intentgraph/localization/{eng or zhs}/intents.json` for localization.

## Format of intents/intents.json

```json5
{
   "<monster_id>": {
      "width": 3,    // Width of the graph
      "height": 1.3, // Height of the graph
      "graphs": {
         "0": { // Ascension level, 0 is necessary
                // you may skip some ascension levels
            "damages": [
               {
                  "min": 6
               }
            ],
            "icons": [
               {
                  "x": 2,
                  "y": 0,
                  "type": "ATTACK", // Available values are in AbstractMonster.Intent
                  "damageIndex": 0, // Index in damages array above
                  "attackCount": 1,
                  "percentage": 100,
                  "limit": 1
               },
               {
                  "x": 0,
                  "y": 0,
                  "type": "BUFF"
               }
            ],
            "iconGroups": [
               // Wrap a set of icon into a cyan square
               {
                  "x": 0,
                  "y": 0,
                  "w": 1,
                  "h": 1
               }
            ],
            "arrows": [
               {
                  // Format [ start_direction, start_x, start_y, ... ]
                  // If start_direction is 0, it starts horizontally, 1 is vertically.
                  // So [ 0, x0, y0, x1, y2, x3, y4, ... ] creates arrow at
                  // (x0, y0) -> (x1, y0) -> (x1, y2) -> (x3, y2) -> ...
                  // [ 1, x0, y0, y1, x2, y3, ... ] creates arrow at
                  // (x0, y0) -> (x0, y1) -> (x2, y1) -> (x2, y3) -> ...
                  "path": [ 0, 1, 0.5, 2 ]
               },
               {
                  "path": [ 1, 2.3, 1, 1.3, 2.7, 1 ],
                  // If instant = true, it's rendered in red color.
                  "instant": true
               }
            ],
            "labels": [
               {
                  "x": 0.5,
                  "y": 1,
                  "align": "left|right|middle", // Default is middle
                  // If key is not found, show key directly
                  "label": "key in localization/{lang}/intents.json"
               }
            ]
         },
         "2": { // Ascension level 2
            // If this is true, this overwrites all things defined in ascension 0,
            // otherwise, this will be merged with ascension 0
            "overwrite": true,
            // You can overwrite graph size here, too.
            "width": 2,
            "height": 1
         },
         "17": { // Ascension level 17
         }
      }
   }
}
```

## Format of localization/{lang}/intents.json

```json5
{
   "key used in intents/intents.json": "label value",
   "key2 used in intents/intents.json": "label value2",
}
```

## Debug your change

It takes much time to rebuild mod and reopen game. Instead of doing that, you can
Create a dev version of intent graph and reload it during game running.

1. Open root folder of Slay the Spire game. You can see `desktop-1.0.jar` here.
2. Create `intentgraph-intents-dev.json` here, which use same format as `intentgraph/intents/intents.json`.
3. Create `intentgraph-intentStrings-dev.json` here, which use same format as `intentgraph/localization/<eng or zhs>/intents.json`.
4. Run game, with intent graph mod enabled.
5. Modify the `*-dev.json` while the game running.
6. Return to game, use `` ` `` opening console, then use command `reloadintents [overwrite ascension level]`
   to reload intent graph.
   1. You can use `reloadintents -1` to reset to actual ascension level.
7. Once debug is done, copy content of `*-dev.json` to `intents.json` file in your mod.
8. Remove `*-dev.json`, rebuild your mod and test it.

# Intent Graph

## How to add intent to your mod

1. Create file at `intentgraph/intents/intents.json` in your resource folder.
   Make sure it's in that path of your `.jar` file after packaging.
2. Add your intent graph info in that file.
3. If needed, create `intentgraph/localization/{eng or zhs, etc.}/intents.json` for localization.

## Intent Graph Editor (Recommended)

During game, you can open intent graph editor by following steps.

1. Run game with intent graph mod enabled.
2. Use `` ` `` opening console, then use command `fight <encounter id>` to fight the monster you want to edit.
3. Use command `editintent <monster id>` to open editor.
4. Edit, then save.
   1. `Ctrl+Z`: Undo
   2. `Ctrl+Y` or `Ctrl+Shift+Z`: Redo
   3. `Ctrl+S`: Save
5. Saved data will be stored in `intentgraph-intents-dev.json` and `intentgraph-intentStrings-dev.json`. They can be found in Slay the Spire root folder. You can move content to your mod after debug.

## Format of intents/intents.json

```json5
{
   "<monster_id>": {
      "width": 3,    // Width of the graph
      "height": 1.3, // Height of the graph
      "graphList": [
         {
            // id is optional, needed only it's extended
            "id": "a0",
            // The *last* graph that matches condition will be shown.
            // So it's recommended to set condition of first graph to true.
            // By default, it's "true".
            "condition": "true",
            "damages": [
               {
                  "min": 6
               },
               {  // Shows "3~4"
                  "min": 3,
                  "max": 4,
               },
               {
                  // Shows "?"
                  "string": "?"
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
                  "x": 1,
                  "y": 0,
                  "type": "ATTACK",
                  "damageIndex": 3,
                  "attackCountString": "N",
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
         {  // Example of Ascension level 2
            // You may use "ascension", "act", "index" (of monsters),
            // "m.{field}" where field is defined in the monster class and
            // type is boolean or int.
            // Only comparison (>, <, ==, etc.) and logical operators (!, ||, &&)
            // are allowed.
            "condition": "ascension >= 2",
            // Copies everything from the graph with id "a0"
            "extend": "a0",
            // This is only for display, use "condition" to control whether to
            // show it.
            "ascensionLevel": 2,
            // Overwrite damages
            "damages": [
               {
                  "min": 7
               }
            ],
            // Overwrite icons
            "icons": [
               // You may set an item to null if you don't want to overwrite it. 
               null,
               {
                  "x": 0,
                  "y": 0,
                  "type": "DEBUFF"
               }
            ],
            // You can overwrite graph size here, too.
            "width": 2,
            "height": 1
         },
         {  // Ascension level 17
            "condition": "ascension >= 17",
            "ascensionLevel": 17,
         }
      ]
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

If you don't want to use editor, you can manually edit intent file and reload it in game.

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

## Customize Intent Icon

Use `IntentGraphMod.registerIconRenderer()` to customize intent icon rendering.

See [Icon.renderIconImage](https://github.com/chaofan-stsmods/intentgraph/blob/intentgraph/src/main/java/io/chaofan/sts/intentgraph/model/Icon.java#L72) for details.


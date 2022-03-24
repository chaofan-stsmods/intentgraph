# Intent Graph

## How to add intent to your mod

1. Create file at `intentgraph/intents/intents.json` in your resource folder.
   Make sure it's in that path of your `.jar` file after packaging.
2. Add your intent graph info in that file.
3. If needed, create `intentgraph/localization/<eng or zhs>/intents.json` for localization.

## Format of intents.json

```json5
{
   "<monster_id>": {
      "width": 3,    // Width of the graph
      "height": 1.3, // Height of the graph
      "a0": {        // a0, a1, a2 are in the same format
                     // They are use in different ascension level
         "damages": [
            {
               "min": 6
            }
         ],
         "icons": [
            {
               "x": 2,
               "y": 0,
               "type": "ATTACK",
               "damageIndex": 0,
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
               // So [ 0, x_0, y_0, x_1, y_2, x_3, y_4, ... ]
               // y_1 = y_0, x_2 = x_1, y_3 = y_2, ...
               // [ 1, x_0, y_0, y_1, x_2, y_3, ... ]
               // x_1 = x_0, y_2 = y_1, x_3 = x_2, ...
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
               "label": "key in localization/{lang}/intents.json"
            }
         ]
      },
      "a1": { // Ascension level 2, 3, 4
         // If this is true, this overwrites all things defined in a0,
         // otherwise, this will be merged with a0
         "overwrite": true,
         // You can overwrite graph size here, too.
         "width": 2,
         "height": 1
      },
      "a2": { // Ascension level 17, 18, 19
      }
   }
}
```
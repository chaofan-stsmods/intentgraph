# 意图状态机（Intent Graph）

## 如何在你的 Mod 中添加意图状态机

1. 在你的 `resources` 文件夹中创建文件 `intentgraph/intents/intents.json`。
   确保打包成 `.jar` 后，这个文件的路径仍然是上述路径。
2. 把你的意图状态机信息写进这个文件里。
3. 如果需要本地化，为每种语言创建 `intentgraph/localization/{eng 或 zhs 等}/intents.json`。

## 意图状态机编辑器（推荐）

在游戏中，你可以通过下面的步骤打开意图状态机编辑器：

1. 以游玩Mod的方式运行游戏，并启用意图状态机。
2. 使用按键 `` ` `` 打开控制台，然后使用命令 `fight <encounter id>` 来进入你想编辑的怪物战斗。
3. 使用命令 `editintent <monster id>` 打开编辑器。
4. 编辑并保存。
   1. `Ctrl+Z`：撤销
   2. `Ctrl+Y` 或 `Ctrl+Shift+Z`：重做
   3. `Ctrl+S`：保存
5. 保存的数据会写入 `intentgraph-intents-dev.json` 和 `intentgraph-intentStrings-dev.json`，它们位于 Slay the Spire 根目录。调试完成后，你可以把内容移动到你的 Mod 中。

## `intents/intents.json` 的格式

```json5
{
   "<monster_id>": {
      "width": 3,    // 图的宽度
      "height": 1.3, // 图的高度
      "graphList": [
         {
            // id 是可选的，仅在被 extend 时需要
            "id": "a0",
            // 会显示*最后一个*满足 condition 的图。
            // 所以建议第一个图的 condition 设置为 true。
            // 默认值为 "true"。
            "condition": "true",
            "damages": [
               {
                  "min": 6
               },
               {  // 显示为 "3~4"
                  "min": 3,
                  "max": 4,
               },
               {
                  // 显示为 "?"
                  "string": "?"
               }
            ],
            "icons": [
               {
                  "x": 2,
                  "y": 0,
                  "type": "ATTACK", // 可用值见 AbstractMonster.Intent
                  "damageIndex": 0,   // 上面 damages 数组中的索引
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
               // 用青色方框把一组图标包起来
               {
                  "x": 0,
                  "y": 0,
                  "w": 1,
                  "h": 1
               }
            ],
            "arrows": [
               {
                  // 格式 [ start_direction, start_x, start_y, ... ]
                  // 如果 start_direction 为 0，则水平方向开始；为 1 则竖直方向开始。
                  // 所以 [ 0, x0, y0, x1, y2, x3, y4, ... ] 会创建一条箭头：
                  // (x0, y0) -> (x1, y0) -> (x1, y2) -> (x3, y2) -> ...
                  // [ 1, x0, y0, y1, x2, y3, ... ] 会创建：
                  // (x0, y0) -> (x0, y1) -> (x2, y1) -> (x2, y3) -> ...
                  "path": [ 0, 1, 0.5, 2 ]
               },
               {
                  "path": [ 1, 2.3, 1, 1.3, 2.7, 1 ],
                  // 如果 instant = true，则用红色渲染。
                  "instant": true
               }
            ],
            "labels": [
               {
                  "x": 0.5,
                  "y": 1,
                  "align": "left|right|middle", // 默认是 middle
                  // 如果在本地化文件中找不到 key，就直接显示 key
                  "label": "localization/{lang}/intents.json 中的 key"
               }
            ]
         },
         {  // 例如：进阶等级 2
            // 你可以使用 "ascension"（进阶）、"act"（章节）、"index"（怪物在战斗中的序号），
            // "m.{field}"，其中 field 是怪物类中定义的字段，并且类型是 boolean 或 int。
            // 只允许比较运算符（>, <, == 等）和逻辑运算符（!, ||, &&）。
            "condition": "ascension >= 2",
            // 从 id 为 "a0" 的图拷贝全部内容
            "extend": "a0",
            // 仅用于显示；是否展示仍由 "condition" 控制。
            "ascensionLevel": 2,
            // 覆盖 damages
            "damages": [
               {
                  "min": 7
               }
            ],
            // 覆盖 icons
            "icons": [
               // 如果不想覆盖某个元素，可以把该项设为 null
               null,
               {
                  "x": 0,
                  "y": 0,
                  "type": "DEBUFF"
               }
            ],
            // 你也可以在这里覆盖图的尺寸
            "width": 2,
            "height": 1
         },
         {  // 进阶等级 17
            "condition": "ascension >= 17",
            "ascensionLevel": 17,
         }
      ]
   }
}
```

## `localization/{lang}/intents.json` 的格式

```json5
{
   "在 intents/intents.json 中使用的 key": "标签文本",
   "在 intents/intents.json 中使用的 key2": "标签文本2",
}
```

## 调试你的修改

如果你不想使用编辑器，可以手动修改意图状态机文件并在游戏中重新加载。

重新打包 Mod 并重开游戏会比较耗时。你可以创建 Intent Graph 的开发版文件，并在游戏运行过程中动态加载。

1. 打开 Slay the Spire 的根目录，你会看到 `desktop-1.0.jar`。
2. 在该目录下创建 `intentgraph-intents-dev.json`，格式与 `intentgraph/intents/intents.json` 相同。
3. 在该目录下创建 `intentgraph-intentStrings-dev.json`，格式与 `intentgraph/localization/<eng 或 zhs>/intents.json` 相同。
4. 运行游戏，并启用 Intent Graph Mod。
5. 在游戏运行过程中修改这些 `*-dev.json` 文件。
6. 回到游戏中，使用按键 `` ` `` 打开控制台，然后使用命令 `reloadintents [overwrite ascension level]`
   来重新加载意图状态机。
   1. 你可以使用 `reloadintents -1` 来重置为当前实际进阶等级。
7. 调试完成后，把 `*-dev.json` 的内容复制到你的 Mod 中对应的 `intents.json` 文件中。
8. 删除这些 `*-dev.json` 文件，重新打包你的 Mod 并进行测试。

## 自定义意图状态机图标

使用 `IntentGraphMod.registerIconRenderer()` 来自定义意图状态机图标的渲染方式。

详细用法可参考 [Icon.renderIconImage](https://github.com/chaofan-stsmods/intentgraph/blob/intentgraph/src/main/java/io/chaofan/sts/intentgraph/model/Icon.java#L72)。

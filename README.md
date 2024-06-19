用于从基岩版转换物品到 Java 版的插件，只能转移部分物品。  
这里放了 2 个插件的源码，分别用于 [LSE](https://github.com/LiteLDev/LegacyScriptEngine) 和 Bukkit。  
> 不要从这个项目的代码中学习，除非你很享受欣赏这个项目💩⛰️代码的过程  
- - -
使用方法：
1. 利用 LSE 插件 `transferItems` 将基岩版玩家物品导出到插件文件夹下的 data 文件夹内，同时使用 `/xuid` 命令获取自己的 XUID
2. 使用 convert.py 数据转换脚本将 data 文件夹下的所有 json 文件转换为 yml 文件并存储到 data_yml 文件夹内
3. 编译 Java 插件 `GetMigratedItems`
4. 在 Java 服务端加载 `GetMigratedItems` 插件，并将 data_yml 文件夹内所有文件复制到插件文件夹下的 data 文件夹内
5. 进入游戏，使用 `/wupin bind <xuid>` 命令绑定玩家 XUID，然后使用 `/wupin open` 打开包含转移物品的物品栏

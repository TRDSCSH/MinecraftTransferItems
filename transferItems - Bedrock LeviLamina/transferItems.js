const itemsFilePath = "plugins/transferItems/items.json";
const migratableItemsFilePath = "plugins/transferItems/migratable_items.json";
const playersDataDir = "plugins/transferItems/data"

const itemsFile = new JsonConfigFile(itemsFilePath);
const migratableItemsFile = new JsonConfigFile(migratableItemsFilePath);
const type2itemid = buildTypeItemidMap();

// mc.regPlayerCmd("cp", "比较背包物品是否是相同物品(测试)", (pl) => {
//     let result = "";
//     const allItems = pl.getInventory().getAllItems();
//     for (let i in allItems) {
//         if (!allItems[i]) continue;
//         if (!allItems[i].type) continue;
//         const tmpItem = newItem(allItems[i].type, allItems[i].aux);
//         if (!tmpItem) {
//             continue;
//         }
//         result += (allItems[i].match(tmpItem) ? "§a■§r" : "§c■§r") + " " + allItems[i].type + "\n";
//     }
//     pl.tell(result);
// });

mc.regPlayerCmd("xuid", "获取自己的 XUID", (pl) => {
    pl.tell("XUID: §a" + pl.xuid);
});

mc.regPlayerCmd("gettool", "获取物品迁移工具", (pl) => {
    const it = mc.newItem("minecraft:potato", 2);
    it.setLore(["神奇的土豆", "可以帮您导出背包物品", "对任意方块长按以使用"]);
    it.setDisplayName("物品迁移工具");
    pl.giveItem(it);
});

mc.listen("onUseItemOn", async(pl, it, bl) => {
    if (it.type == "minecraft:potato") {
        if (pl.isOP() || pl.gameMode == 1) {
            pl.tell("§c创造模式玩家和管理员禁用此功能");
            return;
        }

        const fm = mc.newSimpleForm();
        fm.setTitle("迁移物品");

        let result1 = "";
        let result2 = "";
        const allItems = pl.getInventory().getAllItems();
        for (let i in allItems) {
            if (!allItems[i]) continue;
            if (!allItems[i].type) continue;
            const tmpItem = newItem(allItems[i].type, allItems[i].aux);
            if (!tmpItem) {
                result2 += "§c■§r " + allItems[i].type + "\n";
                continue;
            }
            result1 += (allItems[i].match(tmpItem) ? "§a■§r" : "§c■§r") + " " + allItems[i].type + "\n";
        }
        const result = `以下颜色为§a绿色§r的物品将被迁移:\n${result1}${result2}\n§g在Java服获取你迁移的物品时需要输入XUID，使用 /xuid 命令来查看你的XUID。\n§c迁移操作无法撤销§r`;

        fm.setContent(result);
        fm.addButton("§g■§r 迁移 §g■§r");
        fm.addButton("取消")

        pl.sendForm(fm, async (pl, id) => {
            if (id == null) return;
            if (id == 0) {
                let counter = 0;
                for (let c = 0; c < 12; c++) {
                    let itemTypeAndItemCount = new Object;
                    const allItems = pl.getInventory().getAllItems();
                    for (let i in allItems) {
                        const itType = allItems[i].type + (allItems[i].aux == 0 ? "" : "." + allItems[i].aux);
                        const itCount = allItems[i].count;
                        if (!allItems[i]) continue;
                        if (!allItems[i].type) continue;
                        const tmpItem = newItem(allItems[i].type, allItems[i].aux);
                        if (!tmpItem) {
                            continue;
                        }
                        if (allItems[i].match(tmpItem)) {
                            await clearItem(pl, allItems[i].getNbt(), allItems[i].count);
                            counter += allItems[i].count;
                            if (itemTypeAndItemCount[itType]) {
                                itemTypeAndItemCount[itType] = itemTypeAndItemCount[itType] + itCount;
                            } else {
                                itemTypeAndItemCount[itType] = itCount;
                            }
                        }
                    }
                    pl.refreshItems();
                    writeToJson(pl, itemTypeAndItemCount);
                }
                pl.tell(`§a导出了 ${counter} 个物品`);
            }
        });
    }
    return false;
});

function writeToJson(pl, data) {
    for (let key in data) {
        const itType = key;
        const itCount = data[key];
        const dataFile = new JsonConfigFile(playersDataDir + "/" + pl.xuid + ".json", '{ "player": "' + pl.realName + '" }');
        const count = dataFile.get(itType);
        if (!count) {
            dataFile.set(itType, itCount);
        } else {
            dataFile.set(itType, itCount + count);
        }
    }
}

function newItem(type, aux = 0) {
    // log(type);
    const itemId = type2itemid[`${type}${aux == 0 ? "" : ("." + aux)}`];
    if (!migratableItemsFile.get(`${type}`)) {
        // log(`${type} can not be migrated. (1)`);
        return null;
    } else if (!migratableItemsFile.get(`${type}`).includes(aux)) {
        // log(`${type}${aux == 0 ? "" : ("." + aux)} can not be migrated. (2)`);
        return null;
    }
    if (!itemId) {
        // log(`${type}${aux == 0 ? "" : ("." + aux)} itemId not found.`);
        return null;
    }
    const itemNbt = itemsFile.get(itemId)["nbt"];
    let it;
    if (itemNbt) {
        it = mc.newItem(NBT.parseSNBT(itemNbt));
    } else {
        it = mc.newItem(type, 1);
        if (aux != 0) it.setAux(aux);
    }

    return it;
}

function buildTypeItemidMap() {
    const itemsFile = new JsonConfigFile(itemsFilePath);
    const items = JSON.parse(itemsFile.read());
    let type2itemid = new Object;

    for (let key in items) {
        const item = items[key];
        const itemType = item.type;
        const itemAux = item.aux;
        
        if (!type2itemid[itemType]) {
            type2itemid[itemType] = key; // "minecraft:apple": "10000"
        } else if (itemAux && !type2itemid[`${itemType}.${itemAux}`]) {
            type2itemid[`${itemType}.${itemAux}`] = key; // "minecraft:apple.2": "10000"
        } else {
            type2itemid[`${itemType}.${key}`] = key; // "minecraft:apple.10000": "10000"
        }
    }

    return type2itemid;
}

/**
 * 清除指定数量的NBT物品
 * @param {Player} pl 玩家对象
 * @param {NBT} nbt 物品NBT
 * @param {Number} count 需要清除的数量
 * @returns 未清除的物品数量
 */
async function clearItem(pl, nbt, count) {
    if (!count) return -1;

    const item = mc.newItem(nbt);
    const itemType = item.type;
    const itemAux = item.aux;

    const itemsWithSameAux = new Array;
    const sameItems = new Array;
    const itemsWithDifferentAux = new Array;

    const containers = [pl.getInventory()];
    
    containers.forEach(container => {
        const allItems = container.getAllItems();
        for (let i = 0; i < allItems.length; i++) {
            // 获取玩家物品
            const it = allItems[i];
            if (!it.type) continue;

            // 判断物品是否是目标类型
            if (it.type == itemType && it.aux == itemAux) {
                // log(`${it} ${allItems[i]} ${it === allItems[i]}`)
                if (it.match(mc.newItem(nbt))) {
                    sameItems.push(it.getNbt());
                } else {
                    itemsWithSameAux.push(it.getNbt());
                }
            } else if (it.type == itemType && it.aux != itemAux) {
                itemsWithDifferentAux.push(it.getNbt());
            }
        }
    });

    pl.clearItem(itemType, 64);
    // log(`[clearItem] 清除了所有物品`)

    let clearCount = count;
    for (let i = 0; i < sameItems.length; i++) {
        // log(mc.newItem(sameItems[i]).count)
        pl.giveItem(mc.newItem(sameItems[i]));
        // log(`[clearItem] 给予了相同物品`)
        if (clearCount > 0) {
            clearCount -= pl.clearItem(itemType, clearCount);
            // log(`[clearItem] 清除了相同物品, 还需要清除 ${clearCount} 个`)
        }
        // if (DEBUG) { log(`[clearCount] ${clearCount} i=${i}`); }
    }
    if (clearCount != 0) {
        for (let i = 0; i < sameItems.length; i++) {
            // log(mc.newItem(sameItems[i]).count)
            pl.giveItem(mc.newItem(sameItems[i]));
            // log(`[clearItem] 物品数量不足，退回所有相同物品`)
        }
        pl.tell('物品数量不足, 还需要 ' + (clearCount));
        clearCount = count;
    }

    for (let i = 0; i < itemsWithSameAux.length; i++) {
        pl.giveItem(mc.newItem(itemsWithSameAux[i]));
    }
    for (let i = 0; i < itemsWithDifferentAux.length; i++) {
        pl.giveItem(mc.newItem(itemsWithDifferentAux[i]));
    }

    pl.refreshItems();

    await new Promise(function (resolve, reject) { setTimeout(function () { resolve(); }, 5); });

    // if (DEBUG) log(`Result = ${!clearCount}`);
    return clearCount;
}
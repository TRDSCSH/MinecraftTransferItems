import os
import json
import yaml

def load_mapping(map_file):
    with open(map_file, 'r', encoding='utf-8') as f:
        return json.load(f)

def replace_keys(data, mapping):
    if isinstance(data, dict):
        return {mapping.get(k, k): replace_keys(v, mapping) for k, v in data.items()}
    elif isinstance(data, list):
        return [replace_keys(i, mapping) for i in data]
    else:
        return data

def convert_json_to_yml(source_dir, target_dir, map_file):
    # 加载键映射关系
    mapping = load_mapping(map_file)

    # 创建目标文件夹，如果它不存在
    os.makedirs(target_dir, exist_ok=True)

    # 遍历源文件夹中的所有文件
    for filename in os.listdir(source_dir):
        if filename.endswith(".json"):
            source_file = os.path.join(source_dir, filename)
            target_file = os.path.join(target_dir, os.path.splitext(filename)[0] + ".yml")

            # 读取JSON文件
            with open(source_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            # 替换键
            data = replace_keys(data, mapping)

            # 写入YML文件
            with open(target_file, 'w', encoding='utf-8') as f:
                yaml.dump(data, f, allow_unicode=True, default_flow_style=False)

# 示例用法
source_dir = 'data'
target_dir = 'data_yml'
map_file = 'map.json'
convert_json_to_yml(source_dir, target_dir, map_file)

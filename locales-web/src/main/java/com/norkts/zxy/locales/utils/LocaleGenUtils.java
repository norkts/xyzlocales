package com.norkts.zxy.locales.utils;

import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * generate language config json from key:value
 * parse key language content from config json
 */
public class LocaleGenUtils {
    public static List<String> treeToRows(Map<String, Object> json, String parent){
        List<String> lines = Lists.newArrayList();
        for(String key : json.keySet()) {

            Object sub = json.get(key);
            if(sub instanceof String) {
                if(parent.isEmpty()){
                    lines.add(key + ":" + sub);
                }else{
                    lines.add(parent+ "."+ key + ":" + sub);
                }

            }else{
                Map<String, Object> moudel = (Map<String, Object>)sub;
                if(parent.isEmpty()){
                    lines.addAll(treeToRows(moudel, key));
                }else{
                    lines.addAll(treeToRows(moudel, parent + "." + key));
                }
            }
        }

        return lines;
    }

    public static Map<String, Object> rowToTree(List<String> lines){
        Map<String, Object> maps = Maps.newHashMap();

        for(String line : lines){
            String key = line.substring(0, line.indexOf(":"));
            String value = line.substring(line.indexOf(":") + 1);
            Map<String, Object> map = getAndGenerateMapByKey(maps, key);
            map.put(key.substring(key.lastIndexOf(".") + 1), value);
        }

        return maps;
    }

    private static Map<String, Object> getAndGenerateMapByKey(Map<String, Object> map, String key){
        String[] keys = key.split("\\.");

        Map<String, Object> maps = map;
        for(int i = 0; i < keys.length - 1; i++) {
            maps = (Map<String, Object>)maps.computeIfAbsent(keys[i], k -> Maps.newHashMap());
        }

        return maps;
    }
}

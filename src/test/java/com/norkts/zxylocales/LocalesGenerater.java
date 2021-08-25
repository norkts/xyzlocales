package com.norkts.zxylocales;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.norkts.zxylocales.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

public class LocalesGenerater {

    private static Map<String, String> numKeyMapping = Maps.newLinkedHashMap();

    @BeforeAll
    static void loadNumKeyMapping() throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream("src/main/resources/num-key-mapping.txt"), StandardCharsets.UTF_8);
        lines.remove(0);
        for(String line : lines) {
            if(StringUtils.isWhitespace(line)){
                continue;
            }

            String key = line.substring(0, line.indexOf("\t")).trim();
            String value = line.substring(line.indexOf("\t") + 1).trim();

            numKeyMapping.put(key.trim(), value.trim());
        }

        System.out.println("loadNumKeyMapping finished");
    }

    @Test
    public void generateEn() throws IOException {
        generateLocales("en");
    }

    @Test
    public void generateZh() throws IOException {
        generateLocales("zh");
    }

    public static void generateLocales(String lng) throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream("src/main/resources/"+lng+".txt"), StandardCharsets.UTF_8);
        lines.remove(0);

        Map<String, Object> langMapForVue = Maps.newLinkedHashMap();
        Map<String, String> langMapForReact = Maps.newLinkedHashMap();

        for(String line : lines) {
            if(StringUtils.isBlank(line) || !line.contains("\t")) {
                break;
            }

            String key = line.substring(0, line.indexOf("\t")).trim();
            String value = line.substring(line.indexOf("\t") + 1).trim();

            String configKey = numKeyMapping.get(key);
            if(StringUtils.isBlank(configKey)) {
                System.out.println(key + " mapping not found");
                continue;
            }
            putMapByKey(langMapForVue, configKey, value);

            List<String> keys = Lists.newArrayList();
            for(String keyPart : configKey.split("\\.")){
                if("-".equals(keyPart)){
                    continue;
                }

                keys.add(keyPart);
            }
            langMapForReact.put(StringUtils.join(keys, "."), value);
        }

        List<Map.Entry<String, String>> globals = Lists.newArrayList();
        if("en".equals(lng)){
            globals.add(new AbstractMap.SimpleEntry<>("global.ClickConnectWallet", "click connect wallet"));
            globals.add(new AbstractMap.SimpleEntry<>("global.Confirm", "Confirm"));
            globals.add(new AbstractMap.SimpleEntry<>("global.Loading", "Loading"));
            globals.add(new AbstractMap.SimpleEntry<>("global.NoMoreInfo", "No more info"));
            globals.add(new AbstractMap.SimpleEntry<>("global.NumberError", "Error, please retry"));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradeSuccessMsg","Successful"));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradeFailedMsg","Error, please retry."));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradePendingMsg","Pending ……"));
        }else{
            globals.add(new AbstractMap.SimpleEntry<>("global.ClickConnectWallet", "连接钱包"));
            globals.add(new AbstractMap.SimpleEntry<>("global.Confirm", "确认"));
            globals.add(new AbstractMap.SimpleEntry<>("global.Loading", "加载中"));
            globals.add(new AbstractMap.SimpleEntry<>("global.NoMoreInfo", "没有更多了"));
            globals.add(new AbstractMap.SimpleEntry<>("global.NumberError", "数值有误，请重新输入"));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradePendingMsg","正在执行，请稍候"));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradeSuccessMsg","执行成功"));
            globals.add(new AbstractMap.SimpleEntry<>("global.TradeFailedMsg","执行失败，请重试"));
        }
        globals.add(new AbstractMap.SimpleEntry<>("lng", lng));

        for(Map.Entry<String, String> entry : globals) {
            putMapByKey(langMapForVue, entry.getKey(), entry.getValue());
            langMapForReact.put(entry.getKey(), entry.getValue());
        }

        File vue = new File("src/main/resources/vue/"+lng+".json");
        if(!vue.getParentFile().exists()) {
            vue.getParentFile().mkdirs();
        }


        File react = new File("src/main/resources/react/"+lng+".json");

        if(!react.getParentFile().mkdirs()) {
            react.getParentFile().mkdirs();
        }

        IOUtils.writeString(vue.getAbsolutePath(), JSON.toJSONString(langMapForVue, SerializerFeature.PrettyFormat));
        IOUtils.writeString(react.getAbsolutePath(), JSON.toJSONString(langMapForReact, SerializerFeature.PrettyFormat));
    }

    public static Map<String,Object> putMapByKey(Map<String,Object> map, String key, String value){
        String[] keys = key.split("\\.");
        Map<String, Object> subMap = map;
        for(int i = 0; i < keys.length - 1; i++){
            if("-".equals(keys[i])){
                continue;
            }

            subMap = (Map<String, Object>)subMap.computeIfAbsent(keys[i], k -> Maps.newLinkedHashMap());
        }

        subMap.put(keys[keys.length - 1], value);

        return subMap;
    }

    public static List<String> treeToRow(String lng, Predicate<String> predicate) throws IOException {
        String content = IOUtils.readAsString(new FileInputStream("src/main/resources/"+lng+".json"), StandardCharsets.UTF_8);
        JSONObject json = JSON.parseObject(content);
        return treeToRow(json, null, predicate);
    }

    private static List<String> treeToRow(Map<String, Object> tree, String parentKey, Predicate<String> predicate){
        List<String> lists = Lists.newArrayList();
        for(String key : tree.keySet()){
            Object val = tree.get(key);
            String subKey = StringUtils.isBlank(parentKey) ? key : parentKey + "." + key;
            if(val instanceof String){
                if(predicate.test(subKey)) {
                    lists.add(subKey + "\t" + val);
                }
            }else{
                lists.addAll(treeToRow((Map<String, Object>)val, subKey, predicate));
            }
        }

        return lists;
    }
}

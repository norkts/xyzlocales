package com.norkts.zxylocales;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.norkts.zxylocales.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        Map<String, Object> zhMap = Maps.newHashMap();
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
            putMapByKey(zhMap, configKey, value);
        }

        if("en".equals(lng)){
            putMapByKey(zhMap,"global.ClickConnectWallet", "click connect wallet");
            putMapByKey(zhMap,"global.Confirm", "Confirm");
            putMapByKey(zhMap,"global.Loading", "Loading");
            putMapByKey(zhMap,"global.NoMoreInfo", "No more info");
            putMapByKey(zhMap,"global.NumberError", "Error, please retry");
            putMapByKey(zhMap,"global.TradeSuccessMsg","Successful");
            putMapByKey(zhMap,"global.TradeFailedMsg","Error, please retry.");
            putMapByKey(zhMap,"global.TradePendingMsg","Pending ……");
        }else{
            putMapByKey(zhMap,"global.ClickConnectWallet", "连接钱包");
            putMapByKey(zhMap,"global.Confirm", "确认");
            putMapByKey(zhMap,"global.Loading", "加载中");
            putMapByKey(zhMap,"global.NoMoreInfo", "没有更多了");
            putMapByKey(zhMap,"global.NumberError", "数值有误，请重新输入");
            putMapByKey(zhMap,"global.TradePendingMsg","正在执行，请稍候");
            putMapByKey(zhMap,"global.TradeSuccessMsg","执行成功");
            putMapByKey(zhMap,"global.TradeFailedMsg","执行失败，请重试");
        }
        putMapByKey(zhMap,"lng", lng);

        IOUtils.writeString("src/main/resources/"+lng+".json", JSON.toJSONString(zhMap, SerializerFeature.PrettyFormat));
    }

    public static Map<String,Object> putMapByKey(Map<String,Object> map, String key, String value){
        String[] keys = key.split("\\.");
        Map<String, Object> subMap = map;
        for(int i = 0; i < keys.length - 1; i++){
            if("-".equals(keys[i])){
                continue;
            }

            subMap = (Map<String, Object>)subMap.computeIfAbsent(keys[i], k -> Maps.newHashMap());
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

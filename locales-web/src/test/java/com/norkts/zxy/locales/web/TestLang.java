package com.norkts.zxy.locales.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class TestLang {
    @Test
    public void testLangParse() throws IOException {
        String lang = IOUtils.toString(new FileInputStream("/Users/norkts/Documents/documents/work-fz/norkts-test/zh.json"), StandardCharsets.UTF_8);
        JSONObject langConfig = JSON.parseObject(lang);

        List<String> lines = treeToRows(langConfig, "");
        System.out.println(StringUtils.join(lines, "\n"));

        System.out.println(JSON.toJSONString(rowToTree(lines)));
    }

    @Test
    public void testLangGenerateEn() throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream("/Users/norkts/Documents/documents/work-fz/norkts-test/en.key"), Charset.forName("GBK"));

        lines.add("global.ClickConnectWallet:Click connect wallet");
        lines.add("global.NoMoreInfo:No more info");
        lines.add("global.Loading:Loading");
        lines.add("lng:en");

        System.out.println(JSON.toJSONString(rowToTree(lines), SerializerFeature.PrettyFormat));
    }

    @Test
    public void testLangGenerateZh() throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream("/Users/norkts/Documents/documents/work-fz/norkts-test/zh.key"), Charset.forName("GBK"));

        lines.add("global.ClickConnectWallet:连接钱包");
        lines.add("global.NoMoreInfo:没有更多了");
        lines.add("global.Loading:加载中");
        lines.add("lng:zh");

        System.out.println(JSON.toJSONString(rowToTree(lines), SerializerFeature.PrettyFormat));
    }

    public static List<String> treeToRows(JSONObject json, String parent){
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
                JSONObject moudel = json.getJSONObject(key);
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
            if(keys[i].equals("-")){
                continue;
            }

            maps = (Map<String, Object>)maps.computeIfAbsent(keys[i], k -> Maps.newHashMap());
        }

        return maps;
    }
}

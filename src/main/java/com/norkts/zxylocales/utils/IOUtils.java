package com.norkts.zxylocales.utils;

import com.google.common.collect.Lists;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IOUtils {
    public static String readAsString(InputStream reader, Charset charset) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buff = new byte[256];
        while (true) {
            int len = reader.read(buff);
            if(len <= 0 ) {
                break;
            }
            bos.write(buff, 0, len);
        }
        bos.close();
        return bos.toString(charset.name());
    }

    public static List<String> readLines(InputStream inputStream, Charset charset) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream,charset));
        byte[] buff = new byte[256];
        List<String> lines = Lists.newArrayList();
        while (true) {
            String line = reader.readLine();
            if(line == null ) {
                break;
            }
            lines.add(line);
        }
        return lines;
    }

    public static void writeString(String file, String json) throws IOException {
        FileOutputStream writer = new FileOutputStream(file);
        writer.write(json.getBytes(StandardCharsets.UTF_8));
        writer.close();
    }
}

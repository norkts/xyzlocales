package com.norkts.zxylocales.utils;

import com.google.common.collect.Lists;

import java.io.*;
import java.nio.charset.Charset;
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
        FileWriter writer = new FileWriter(file);
        writer.write(json);
        writer.close();
    }
}

package com.denidove.Logistics.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class NIO {

    public static String readFile(Path path) {
        int ch;
        String s = "";
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        // Ниже конструкция try-with-resources:
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            // посимвольное чтение:
            /*
            while ((ch = reader.read()) != -1) { // или line = reader.readLine()) != null
                char c = (char)ch;
                s += c;
            }*/

            // построчное чтение:
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
                // здесь добавили append("\n") для более красивого отбражения, но можно обойтись и без этого
            }
        } catch (IOException e) {}
        return stringBuilder.toString();
    }

    public static String readResource(String path) {
        String htmlFile = "";

        try (InputStream inputStream = NIO.class.getResourceAsStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
             htmlFile = reader.lines().collect(Collectors.joining("\n"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    return htmlFile;
    }
}

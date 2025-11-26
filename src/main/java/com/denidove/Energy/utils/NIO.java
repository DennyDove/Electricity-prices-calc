package com.denidove.Energy.utils;

import com.denidove.Energy.EnergyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class NIO {

    // Метод для получения пути, относительного к JAR-файлу (нашего приложения)
    // Нужен чтобы открывать файлы по относительному пути (относительно JAR файла нашего приложения)
    public static File getAppDirectory() {
        try {
            String cp = System.getProperty("java.class.path");

            // Возможен список путей через ; (Windows) или : (Linux)
            // Берем первый элемент
            String first = cp.split(File.pathSeparator)[0];

            File file = new File(first);

            // Если это JAR → берем его директорию
            if (file.isFile()) {
                return file.getParentFile();
            }
            // Иначе - IDE (IDE classes dir)
            return file;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Старый вариант - нерабочий
    public static File getJarDir() {
        try {
            return new File(EnergyApplication.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI())
                    .getParentFile();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка определения директории JAR", e);
        }
    }

    // Чтение текстовых файлов
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

}

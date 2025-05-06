package org.example;

import java.io.*;

public class StringSerializer {
    public static byte[] serializeStrings(String... strings) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream dataStream = new DataOutputStream(byteStream)) {

            // Записываем количество строк
            dataStream.writeInt(strings.length);

            // Записываем каждую строку (сначала длину, потом данные)
            for (String str : strings) {
                dataStream.writeUTF(str);
            }

            return byteStream.toByteArray();
        }
    }

    public static String[] deserializeStrings(byte[] bytes) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             DataInputStream dataStream = new DataInputStream(byteStream)) {

            // Читаем количество строк
            int count = dataStream.readInt();
            String[] strings = new String[count];

            // Читаем каждую строку
            for (int i = 0; i < count; i++) {
                strings[i] = dataStream.readUTF();
            }

            return strings;
        }
    }
}

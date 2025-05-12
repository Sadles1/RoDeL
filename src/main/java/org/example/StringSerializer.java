package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

public class StringSerializer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static byte[] serializeStrings(String... strings) {
        try {
            return mapper.writeValueAsBytes(strings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] deserializeStrings(byte[] bytes) {
        try {
            return mapper.readValue(bytes, String[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package com.company;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PrintError {

    enum PartWordErr {
        one, two
    }

    private static final Map<PartWordErr, Integer> indexErr;

    static {
        indexErr = new HashMap<>();

        indexErr.put(PartWordErr.one, 0);
        indexErr.put(PartWordErr.two, 1);
    }

    private static final String[] err = {"Error: File ", " does not exist."};

    static void writeErr(Exception message, String fileLog) {
        try {
            FileWriter fileWriter = new FileWriter(fileLog);
            fileWriter.write(message.getMessage());
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println(err[indexErr.get(PartWordErr.one)] + fileLog + err[indexErr.get(PartWordErr.two)]);
        }
    }

    static void writeErr(String message, String fileLog) {
        try {
            FileWriter fileWriter = new FileWriter(fileLog);
            fileWriter.write(message);
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println(err[indexErr.get(PartWordErr.one)] + fileLog + err[indexErr.get(PartWordErr.two)]);
        }
    }
}

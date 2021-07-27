package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class ParseConfigFile {

    enum Parameters {
        input, param, output
    }

    String fileIn;
    String fileParam;
    String fileOut;

    final String GrammarEqual = "=";

    private static final Map<Parameters, String> map;
    private static final String GRAM = "=";

    static {
        map = new HashMap<>();

        map.put(Parameters.input, "INPUT_FILE:");
        map.put(Parameters.param, "INPUT_PARAM:");
        map.put(Parameters.output, "OUTPUT_FILE:");
    }

    void parseFile(FileInputStream fileCfg, String fileLog) {
        String str;
        String strDelete = "\r\n"; //Разделение
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[fileCfg.available()];
            fileCfg.read(buffer);
            bytes.write(buffer);
        } catch (IOException e) {
            PrintError.writeErr(e, fileLog);
        }
        str = bytes.toString();
        String[] parameters = str.split(strDelete);

        for (String item : parameters) {
            if (item.startsWith(map.get(Parameters.input))) {
                if (fileIn == null) {
                    fileIn = item.substring(map.get(Parameters.input).length());
                }
            }
            if (item.startsWith(map.get(Parameters.param))) {
                if (fileParam == null) {
                    fileParam = item.substring(map.get(Parameters.param).length());

                }
            }
            if (item.startsWith(map.get(Parameters.output))) {
                if (fileOut == null) {
                    fileOut = item.substring(map.get(Parameters.output).length());

                }
            }
        }
    }

}
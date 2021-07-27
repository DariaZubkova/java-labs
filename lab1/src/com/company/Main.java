package com.company;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        String fileLog = "log.txt";

        if (args.length != 1) {
            PrintError.writeErr("Error: Incorrect parameters in command string.", fileLog);
        } else {
            String fCfg = args[0];
            try {
                ParseConfigFile CFGParser = new ParseConfigFile();
                //Открываем config файл
                FileInputStream fileCfg = new FileInputStream(fCfg);
                //Разбор config файла
                CFGParser.parseFile(fileCfg, fileLog);

                //Открываем Input файл
                FileInputStream fileIn = new FileInputStream(CFGParser.fileIn);
                //Открываем Input файл
                FileInputStream fileParam = new FileInputStream(CFGParser.fileParam);
                //Открываем Output файл
                FileOutputStream fileOut = new FileOutputStream(CFGParser.fileOut);

                //Делаем подстановку
                TableSubstitutions lTable = new TableSubstitutions();
                ArrayList<Byte> arrayOut = lTable.doTableSubstitutions(fileIn, fileParam, fileLog);

                if (arrayOut != null) {
                    //Записываем результат
                    OutputStreamWriter writer = new OutputStreamWriter(fileOut);
                    BufferedWriter bufferOut = new BufferedWriter(writer);
                    for (Byte aByte : arrayOut) {
                        bufferOut.write(aByte);
                        bufferOut.flush();
                    }
                }

                fileIn.close();
                fileParam.close();
                fileOut.close();
                fileCfg.close();
            } catch (IOException e) {
                PrintError.writeErr(e, fileLog);
            }
        }
    }
}

package com.company;

import java.io.*;
import java.util.ArrayList;

public class TableSubstitutions {

    ArrayList<Byte> doTableSubstitutions(FileInputStream fileIn, FileInputStream fileParam, String fileLog) {

        ArrayList<Byte> arrayOut = new ArrayList<Byte>();
        //Массив элементов входного файла
        ArrayList<Byte> arrayIn = new ArrayList<Byte>();
        //Массив пар
        ArrayList<Byte> arrayParam = new ArrayList<Byte>();

        int paramElements = 2; //Пары

        try {
            int endRead = -1; //Закончились символы

            //Массив элементов входного файла
            byte in = (byte) fileIn.read();
            while (in != endRead) {
                arrayIn.add(in);
                in = (byte) fileIn.read();
            }

            //Массив пар
            in = (byte) fileParam.read();
            while (in != endRead) {
                if (in != ' ' && in != '\r' && in != '\n') {
                    arrayParam.add(in);
                }
                in = (byte) fileParam.read();
            }
        } catch (IOException e) {
            PrintError.writeErr(e, fileLog);
        }

        //Проверка на количество пар
        int limitNumberSymbol = 256; //Максимальное количество пар
        if ((arrayParam.size() / paramElements) > limitNumberSymbol) {
            PrintError.writeErr("Error: The number of param in the file exceeds 256.", fileLog);
            return arrayOut;
        }

        //Проверка: содержатся ли первые элементы пар во входящем файле
        for (int pos1 = 0; pos1 < arrayParam.size(); pos1 += paramElements) {
            boolean check = false;
            for (int pos2 = 0; pos2 < arrayIn.size(); pos2++) {
                if (arrayParam.get(pos1).equals(arrayIn.get(pos2))) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                PrintError.writeErr("Error: The element is not contained in the input file.", fileLog);
                return arrayOut;
            }

        }

        //Проверка: повторяются ли первые элементы из пар
        ArrayList<Byte> copyArrayParam = new ArrayList<Byte>(arrayParam);
        int numberSymbolRepeat = 1;

        for (int pos1 = 0; pos1 < arrayParam.size(); pos1 += paramElements) {
            int num = 0;
            for (int pos2 = 0; pos2 < copyArrayParam.size(); pos2 += paramElements) {
                if (arrayParam.get(pos1).equals(copyArrayParam.get(pos2)))
                    num++;
            }
            if (num > numberSymbolRepeat) {
                PrintError.writeErr("Error: The first elements in the files are repeated.", fileLog);
                return arrayOut;
            }
        }

        //Совершаем подстановку
        arrayOut.addAll(arrayIn); //Копируем содержимое входящего файла в файл результата

        for (int pos1 = 0; pos1 < arrayParam.size(); pos1 += paramElements) {
            for (int pos2 = 0; pos2 < arrayOut.size(); pos2++) {
                if (arrayParam.get(pos1).equals(arrayOut.get(pos2))) {
                    arrayOut.set(pos2, arrayParam.get(pos1 + 1));
                }
            }
        }

        return arrayOut;
    }

}

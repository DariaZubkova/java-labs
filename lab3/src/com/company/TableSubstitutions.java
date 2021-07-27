package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;
import java.io.*;
import java.util.*;

import ru.spbstu.pipeline.Executor;

public class TableSubstitutions implements Executor {

    enum modes{
        ENCODE, DECODE
    };

    private Object inputData;
    private Object outputData;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private Status status = Status.OK;
    private Logger logger;
    private String fileParam = null;
    private int num = 0;
    private String mode = null;
    private Producer.DataAccessor dataAccessor = null;
    private HashMap dataAccessorType = new HashMap<String, DataAccessor>();
    private Set<String> DEFAULT_TYPES = new HashSet<>();
    private String MyType;
    private String outputType;

    public TableSubstitutions(String configFile, Logger logger){
        MyType = byte[].class.getCanonicalName();
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        dataAccessorType.put(byte[].class.getCanonicalName(), new DataAccessorByte());
        dataAccessorType.put(String.class.getCanonicalName(), new DataAccessorString());
        dataAccessorType.put(char[].class.getCanonicalName(), new DataAccessorChar());

        producers = new ArrayList<>();
        consumers = new ArrayList<>();
        this.logger = logger;
        ParseTable parser = new ParseTable(configFile, logger);
        fileParam = parser.paramFile();
        if(fileParam == null){
            status = Status.ERROR;
            logger.log("Error paramfile null");
            return;
        }
        num = parser.num();
        if(num == 0){
            status = Status.ERROR;
            logger.log("Error num in paramfile null");
        }
        mode = parser.mode();
        if(mode == null){
            status = Status.ERROR;
            logger.log("Error mode in paramfile null");
        }
    }

    @NotNull
    public Status status(){
        return this.status;
    }

    @Override
    public long loadDataFrom(@NotNull Producer producer){
        if(dataAccessor!=null) {
            inputData = dataAccessor.get();
            return dataAccessor.size();
        }

        return 0;
    }

    public void run(){
        try {
            for (Consumer consumer: consumers){
                //consumer.loadDataFrom(this);
                if (mode.equals(modes.ENCODE.toString())) {
                    FileInputStream fin = new FileInputStream(fileParam);

                    //Массив пар
                    byte[] paramBuffer = new byte[fin.available()];
                    fin.read(paramBuffer);
                    int numberRes = 0;

                    ArrayList<Byte> paramByte = new ArrayList<Byte>();
                    for (byte b : paramBuffer) {
                        if (b != ' ' && b != '\r' && b != '\n') {
                            paramByte.add(numberRes, (Byte) b);
                            numberRes++;
                        }
                    }

                    //Входные данные
                    byte[] inBuffer = (byte[]) inputData;
                    ArrayList<Byte> inByte = new ArrayList<Byte>();
                    for (int i = 0; i < inBuffer.length; i++) {
                        if (inBuffer[i] != 0)
                            inByte.add(i, (Byte) inBuffer[i]);
                    }

                    //Максимальное количество пар
                    int paramElements = 2;
                    int limitNumberSymbol = 256;
                    if ((paramByte.size() / paramElements) > limitNumberSymbol) {
                        status = Status.EXECUTOR_ERROR;
                        logger.log("Error: The number of param in the file exceeds 256");
                        return;
                    }

                    //Проверка: повторяются ли первые элементы из пар
                    ArrayList<Byte> copyByte = new ArrayList<Byte>(paramByte);
                    int numberSymbolRepeat = 1;
                    for (int pos1 = 0; pos1 < paramByte.size(); pos1 += paramElements) {
                        int number = 0;
                        for (int pos2 = 0; pos2 < copyByte.size(); pos2 += paramElements) {
                            if (paramByte.get(pos1).equals(copyByte.get(pos2)))
                                number++;
                        }
                        if (number > numberSymbolRepeat) {
                            status = Status.EXECUTOR_ERROR;
                            logger.log("Error: The first elements in the files are repeated");
                            return;
                        }
                    }

                    //Совершаем подстановку
                    for (int pos2 = 0; pos2 < inByte.size(); pos2++) {
                        for (int pos1 = 0; pos1 < paramByte.size(); pos1 += paramElements) {
                            if (paramByte.get(pos1).equals(inByte.get(pos2))) {
                                inByte.set(pos2, paramByte.get(pos1 + 1));
                                pos2++;
                            }
                        }
                    }
                    byte[] outBuffer = new byte[inByte.size()];
                    for (int i = 0; i < inByte.size(); i++) {
                        outBuffer[i] = inByte.get(i).byteValue();
                    }
                    outputData = outBuffer;
                }
                else {
                    FileInputStream fin = new FileInputStream(fileParam);

                    //Массив пар
                    byte[] paramBuffer = new byte[fin.available()];
                    fin.read(paramBuffer);
                    int numberRes = 0;

                    ArrayList<Byte> paramByte = new ArrayList<Byte>();
                    for (byte b : paramBuffer) {
                        if (b != ' ' && b != '\r' && b != '\n') {
                            paramByte.add(numberRes, (Byte) b);
                            numberRes++;
                        }
                    }

                    //Входные данные
                    byte[] inBuffer = (byte[]) inputData;
                    ArrayList<Byte> inByte = new ArrayList<Byte>();
                    for (int i = 0; i < inBuffer.length; i++) {
                        if (inBuffer[i] != 0)
                            inByte.add(i, (Byte) inBuffer[i]);
                    }

                    //Максимальное количество пар
                    int paramElements = 2;
                    int limitNumberSymbol = 256;
                    if ((paramByte.size() / paramElements) > limitNumberSymbol) {
                        status = Status.EXECUTOR_ERROR;
                        logger.log("Error: The number of param in the file exceeds 256");
                        return;
                    }

                    //Проверка: повторяются ли первые элементы из пар
                    ArrayList<Byte> copyByte = new ArrayList<Byte>(paramByte);
                    int numberSymbolRepeat = 1;
                    for (int pos1 = 0; pos1 < paramByte.size(); pos1 += paramElements) {
                        int number = 0;
                        for (int pos2 = 0; pos2 < copyByte.size(); pos2 += paramElements) {
                            if (paramByte.get(pos1).equals(copyByte.get(pos2)))
                                number++;
                        }
                        if (number > numberSymbolRepeat) {
                            status = Status.EXECUTOR_ERROR;
                            logger.log("Error: The first elements in the files are repeated");
                            return;
                        }
                    }

                    //Совершаем подстановку
                    for (int pos2 = 0; pos2 < inByte.size(); pos2++) {
                        for (int pos1 = 0; pos1 < paramByte.size(); pos1 += paramElements) {
                            if (paramByte.get(pos1 + 1).equals(inByte.get(pos2))) {
                                inByte.set(pos2, paramByte.get(pos1));
                                pos2++;
                            }
                        }
                    }
                    byte[] outBuffer = new byte[inByte.size()];
                    for (int i = 0; i < inByte.size(); i++) {
                        outBuffer[i] = inByte.get(i).byteValue();
                    }
                    outputData = outBuffer;
                }
                long check = consumer.loadDataFrom(this);
                if (check == 0) {
                    status = Status.READER_ERROR;
                    return;
                }
                consumer.run();
                this.status = consumer.status();
                if (status != Status.OK) {
                    logger.log("Error in executor");
                    return;
                }
            }
        } catch(IOException e){
            status = Status.EXECUTOR_ERROR;
            logger.log("Error can not read paramfile");
        }
    }

    public void addConsumer(@NotNull Consumer consumer) {
        consumers.add(consumer);
    }

    public void addConsumers(List<Consumer> consumers){
        for (Consumer consumer: consumers)
            addConsumer(consumer);
    }

    public void addProducer(@NotNull Producer producer){
        Set<String> producersOutputDataTypes = producer.outputDataTypes();

        for (String i : producersOutputDataTypes) {
            if(Objects.equals(i, MyType)){
                dataAccessor = producer.getAccessor(i);
            }
        }

        if (dataAccessor == null) {
            status = Status.EXECUTOR_ERROR;
            logger.log("Error add producer executor");
            return;
        }

        producers.add(producer);
    }

    public void addProducers(List<Producer> producers){
        for (Producer producer: producers)
            addProducer(producer);
    }

    @NotNull
    @Override
    public Set<String> outputDataTypes() {
        return DEFAULT_TYPES;
    }

    public final class DataAccessorByte implements Producer.DataAccessor {

        @NotNull
        @Override
        public Object get() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            return temp1;
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            return temp1.length;
        }
    }

    public final class DataAccessorString implements Producer.DataAccessor {

        @Override
        public String get() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            return str;
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            return str.length();
        }
    }

    public final class DataAccessorChar implements Producer.DataAccessor {

        @NotNull
        @Override
        public Object get() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            return str.toCharArray();
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            byte[] temp = (byte[])outputData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            char[] ch = str.toCharArray();
            return ch.length;
        }
    }

    @NotNull
    @Override
    public DataAccessor getAccessor(@NotNull final String typeName) {
        Objects.requireNonNull(dataAccessorType);
        this.outputType = typeName;
        return (DataAccessor) dataAccessorType.get(typeName);
    }

}

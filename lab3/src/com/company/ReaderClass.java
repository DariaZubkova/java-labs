package com.company;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.logging.Logger;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.Reader;
import ru.spbstu.pipeline.Consumer;

class ReaderClass implements Reader {
    private List<Consumer> consumers;
    private Logger logger;
    private String fileIn;
    private int num = 0;
    private Status status = Status.OK;
    private Object inData;
    private String outputType;
    private HashMap dataAccessorType = new HashMap<String, DataAccessor>();
    private Set<String> DEFAULT_TYPES = new HashSet<>();

    public ReaderClass(String configFile, Logger logger){
        DEFAULT_TYPES.add(byte[].class.getCanonicalName());
        DEFAULT_TYPES.add(String.class.getCanonicalName());
        DEFAULT_TYPES.add(char[].class.getCanonicalName());
        dataAccessorType.put(byte[].class.getCanonicalName(), new DataAccessorByte());
        dataAccessorType.put(String.class.getCanonicalName(), new DataAccessorString());
        dataAccessorType.put(char[].class.getCanonicalName(), new DataAccessorChar());


        consumers = new ArrayList<>();
        this.logger = logger;
        ParseFile parser = new ParseFile(configFile, logger);
        fileIn = parser.inputFile();
        if(fileIn == null){
            this.status = Status.READER_ERROR;
            logger.log("Error inputfile null");
            return;
        }
        num = parser.num();
        if(num == 0){
            this.status = Status.READER_ERROR;
            logger.log("Error num in inputfile null");
        }
    }

    @Override
    public void run(){
        try{
            for (Consumer consumer: consumers) {
                FileInputStream fin = new FileInputStream(fileIn);
                byte[] readData = new byte[num];
                while (fin.read(readData, 0, num) != -1) {
                    inData = readData;
                    long check = consumer.loadDataFrom(this);
                    if (check == 0) {
                        status = Status.READER_ERROR;
                        return;
                    }
                    consumer.run();
                    this.status = consumer.status();
                    if (status != Status.OK) {
                        logger.log("Error in reader");
                        return;
                    }
                    readData = new byte[num];
                }
                fin.close();
            }
        } catch(IOException e){
            this.status = Status.READER_ERROR;
            logger.log("Error can not read inputfile " + fileIn);
        }
    }

    @NotNull
    @Override
    public Set<String> outputDataTypes() {
        return DEFAULT_TYPES;
    }

    @Override
    public void addConsumer(@NotNull Consumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void addConsumers(List<Consumer> consumers){
        for (Consumer consumer: consumers)
            addConsumer(consumer);
    }

    public final class DataAccessorByte implements Producer.DataAccessor {

        @NotNull
        @Override
        public Object get() {
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            return temp1;
        }

        @Override
        public long size() {
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            return temp1.length;
        }
    }

    public final class DataAccessorString implements Producer.DataAccessor {

        @NotNull
        @Override
        public Object get() {
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            return str;
        }

        @Override
        public long size() {
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
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
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
            byte[] temp1 = new byte[temp.length];
            System.arraycopy(temp, 0, temp1, 0, temp.length);
            String str = new String(temp1);
            return str.toCharArray();
        }

        @Override
        public long size() {
            Objects.requireNonNull(inData);
            byte[] temp = (byte[])inData;
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

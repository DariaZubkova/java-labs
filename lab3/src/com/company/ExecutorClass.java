package com.company;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.Consumer;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;

import ru.spbstu.pipeline.Executor;

public class ExecutorClass implements Executor {

    private enum Modes {
        ENCODE, DECODE
    }
    private Object inputData;
    private Object outputData;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private Status status = Status.OK;
    private Logger logger;
    private int num = 0;
    private String mode;
    private Producer.DataAccessor dataAccessor;
    private HashMap dataAccessorType = new HashMap<String, DataAccessor>();
    private Set<String> DEFAULT_TYPES = new HashSet<>();
    private String MyType;
    private String outputType;

    public ExecutorClass(String configFile, Logger logger){
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
        ParseExecutor parser = new ParseExecutor(configFile, logger);
        num = parser.num();
        if(num == 0){
            this.status = Status.ERROR;
            logger.log("Error num in inputfile null");
        }
        mode = parser.mode();
        if(mode == null){
            this.status = Status.ERROR;
            logger.log("Error mode in inputfile null");
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
        for (Consumer consumer: consumers){
            if (mode.equals(Modes.ENCODE.toString())) {
                //Входные данные
                byte[] inBuffer = (byte[]) inputData;
                ArrayList<Byte> inByte = new ArrayList<Byte>();
                for (int i = 0; i < inBuffer.length; i++) {
                    if (inBuffer[i] != 0)
                        inByte.add(i, (Byte) inBuffer[i]);
                }

                //Совершаем подстановку
                byte[] outBuffer = new byte[inByte.size() + 2];
                for (int i = 1; i < inByte.size() + 1; i++) {
                    outBuffer[i] = inByte.get(i - 1).byteValue();
                }
                outBuffer[0] = '1';
                outBuffer[inByte.size() + 1] = '1';
                outputData = outBuffer;
            }
            else {
                //Входные данные
                byte[] inBuffer = (byte[]) inputData;
                ArrayList<Byte> inByte = new ArrayList<Byte>();
                for (int i = 0; i < inBuffer.length; i++) {
                    if (inBuffer[i] != 0)
                        inByte.add(i, (Byte) inBuffer[i]);
                }

                //Совершаем подстановку
                byte[] outBuffer = new byte[inByte.size() - 2];
                for (int i = 0; i < inByte.size() - 2; i++) {
                    outBuffer[i] = inByte.get(i + 1).byteValue();
                }
                outputData = outBuffer;
            }
            //consumer.loadDataFrom(this);
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

        //producer.getAccessor(byte[].class.getCanonicalName());
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
            return outputData;
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            return ((byte[]) outputData).length;
        }
    }

    public final class DataAccessorString implements Producer.DataAccessor {

        @Override
        public String get() {
            Objects.requireNonNull(outputData);
            String str = new String((byte[]) outputData);
            return str;
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            String str = new String((byte[]) outputData);
            return str.length();
        }
    }

    public final class DataAccessorChar implements Producer.DataAccessor {

        @NotNull
        @Override
        public Object get() {
            Objects.requireNonNull(outputData);
            String str = new String((byte[]) outputData);
            return str.toCharArray();
        }

        @Override
        public long size() {
            Objects.requireNonNull(outputData);
            String str = new String((byte[]) outputData);
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


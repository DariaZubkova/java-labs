package com.company;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import ru.spbstu.pipeline.logging.Logger;
import ru.spbstu.pipeline.Status;
import ru.spbstu.pipeline.Producer;
import ru.spbstu.pipeline.Writer;

public class WriterClass implements Writer{
    private Object data;
    private List<Producer> producers;
    private String fileOut;
    private int num = 0;
    private FileOutputStream file;
    private Status status = Status.OK;
    private Logger logger;
    private String MyType;
    private Producer.DataAccessor dataAccessor;

    public WriterClass(String configFile, Logger logger){
        MyType = byte[].class.getCanonicalName();

        producers = new ArrayList<>();
        this.logger = logger;
        ParseFile parser = new ParseFile(configFile, logger);
        fileOut = parser.outputFile();
        if(fileOut == null){
            this.status = Status.WRITER_ERROR;
            logger.log("Error outputfile null");
            return;
        }
        num = parser.num();
        if(num == 0){
            this.status = Status.WRITER_ERROR;
            logger.log("Error num on outputfile null");
            return;
        }
        try {
            file = new FileOutputStream(fileOut);
        }
        catch (IOException e) {
            this.status = Status.WRITER_ERROR;
            logger.log("Error can not open outputfile null");
        }
    }

    @NotNull
    @Override
    public Status status() {
        return status;
    }

    @Override
    public long loadDataFrom(@NotNull Producer producer){
        if(dataAccessor!=null) {
            data = dataAccessor.get();
            return dataAccessor.size();
        }

        return 0;
    }

    public void run(){
        if(status != Status.OK) {
            this.status = Status.WRITER_ERROR;
            logger.log("Error status is not OK");
            return;
        }
        writeData();
    }


    private void writeData(){
        if (data == null) {
            this.status = Status.WRITER_ERROR;
            logger.log("Error inputData is null");
            return;
        }

        if(fileOut == null){
            this.status = Status.WRITER_ERROR;
            logger.log("Error outputfile null");
            return;
        }
        try {
            byte[] buff = (byte[])data;
            file.write(buff);
        } catch (IOException e){
            this.status = Status.WRITER_ERROR;
            logger.log("Error can not write outputfile");
        }
    }

    public void addProducer(@NotNull Producer producer){
        Set<String> producersOutputDataTypes = producer.outputDataTypes();

        for (String i : producersOutputDataTypes) {
            if(Objects.equals(i, MyType)){
                dataAccessor = producer.getAccessor(i);
            }
        }

        producers.add(producer);
    }

    public void addProducers(List<Producer> producers){
        for (Producer producer: producers)
            addProducer(producer);
    }

}

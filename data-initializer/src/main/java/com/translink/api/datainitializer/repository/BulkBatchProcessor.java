package com.translink.api.datainitializer.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BulkBatchProcessor {
    @Value(value = "${batch-size}")
    private int batchSize;

    private MongoTemplate mongoTemplate;

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void bulkInsert(Class<?> clazz, List<?> list) {
        long timelapse = Instant.now().toEpochMilli();

        List<List<Object>> batchList = new ArrayList<>();
        List<Object> batch = null;
        for (Object object : list) {
            if(batch == null) {
                batch = new ArrayList<>();
            }

            batch.add(object);
            if (batch.size() == batchSize) {
                batchList.add(batch);
                batch = null;
            }
        }

        if(batch != null) {
            batchList.add(batch);
        }

        batchList.parallelStream()
                .forEach(objects -> insertBatch(clazz, objects));

        log.info("Saved all {} in {}ms", clazz.getSimpleName(), Instant.now().toEpochMilli() - timelapse);
    }

    private void insertBatch(Class<?> clazz, List<Object> list) {
        long timelapse = Instant.now().toEpochMilli();
        int inserted = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, clazz)
                .insert(list)
                .execute()
                .getInsertedCount();

        timelapse = Instant.now().toEpochMilli() - timelapse;
        log.debug("{}: Inserted {} for {}ms", clazz.getSimpleName(), inserted, timelapse);
    }
}

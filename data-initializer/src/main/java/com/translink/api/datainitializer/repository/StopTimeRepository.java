package com.translink.api.datainitializer.repository;

import com.translink.api.datainitializer.repository.model.StopTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopTimeRepository extends MongoRepository<StopTime, String> {
    boolean existsByTripId(String tripId);
    StopTime findByTripIdAndSequence(String tripId, int sequence);
}

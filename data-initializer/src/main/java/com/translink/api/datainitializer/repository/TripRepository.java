package com.translink.api.datainitializer.repository;

import com.translink.api.datainitializer.repository.model.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
}

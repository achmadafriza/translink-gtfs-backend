package com.translink.api.datainitializer.repository;

import com.translink.api.datainitializer.repository.model.Stop;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopRepository extends MongoRepository<Stop, String> {

}

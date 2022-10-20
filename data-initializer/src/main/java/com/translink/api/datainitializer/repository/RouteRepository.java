package com.translink.api.datainitializer.repository;

import com.translink.api.datainitializer.repository.model.Route;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends MongoRepository<Route, String> {
}

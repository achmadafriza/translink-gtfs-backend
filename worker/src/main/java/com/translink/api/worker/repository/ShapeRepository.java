package com.translink.api.worker.repository;

import com.translink.api.worker.repository.model.Shape;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShapeRepository extends MongoRepository<Shape, String> {
}

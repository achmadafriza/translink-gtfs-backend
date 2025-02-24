package com.translink.api.worker.repository;

import com.translink.api.worker.repository.model.Landmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandmarkRepository extends MongoRepository<Landmark, String> {
    Page<Landmark> findByStopIdOrderByRatingDescLatitude(String stopId, Pageable pageable);
}

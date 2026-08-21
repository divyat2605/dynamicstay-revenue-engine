package com.dynamicstay.repository;

import com.dynamicstay.mongo.GuestReview;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GuestReviewRepository extends MongoRepository<GuestReview, String> {
    List<GuestReview> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}

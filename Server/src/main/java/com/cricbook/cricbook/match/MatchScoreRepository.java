package com.cricbook.cricbook.match;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchScoreRepository extends MongoRepository<MatchScore, String> {
}

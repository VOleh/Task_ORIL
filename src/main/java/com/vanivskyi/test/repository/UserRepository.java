package com.vanivskyi.test.repository;

import com.vanivskyi.test.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}

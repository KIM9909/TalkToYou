package com.talktoyou.backend.repository;

import com.talktoyou.backend.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUserName(String userName);

    Optional<User> findByEmail(String email);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);

    Optional<User> findByUserNameAndDeletedAtIsNull(String userName);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByUserNameAndDeletedAtIsNull(String userName);
}
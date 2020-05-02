package com.reactive.examples.repository;

import com.reactive.examples.model.UserCapped;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface UserCappedRepository extends ReactiveMongoRepository<UserCapped,String> {
    @Tailable
    Flux<UserCapped> findUserCappedsBy();
}

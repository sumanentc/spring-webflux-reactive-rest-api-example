package com.reactive.examples.initialize;

import com.reactive.examples.model.User;
import com.reactive.examples.model.UserCapped;
import com.reactive.examples.repository.UserCappedRepository;
import com.reactive.examples.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
@Slf4j
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCappedRepository userCappedRepository;

    @Autowired
    private MongoOperations mongoOperations;
    
    @Override
    public void run(String... args) {
            initialDataSetup();
            createCappedCollection();
            dataSetupForCappedCollection();
    }

    private void createCappedCollection() {
        mongoOperations.dropCollection(UserCapped.class);
        mongoOperations.createCollection(UserCapped.class, CollectionOptions.empty().maxDocuments(20).size(50000).capped());
    }

    private void dataSetupForCappedCollection(){
        Flux<UserCapped> userCappedFlux = Flux.interval(Duration.ofSeconds(5))
                .map(i -> new UserCapped(null,"Stream User " + i,20,1000));
        userCappedRepository.insert(userCappedFlux).subscribe(
                item -> log.info("UserCapped Inserted from CommandLineRunner " + item));

    }

    private List<User> getData(){
        return Arrays.asList(new User("1","Suman Das",30,10000),
                             new User("2","Arjun Das",5,1000),
                             new User("3","Saurabh Ganguly",40,1000000));
    }

    private void initialDataSetup() {
        userRepository.deleteAll()
                .thenMany(Flux.fromIterable(getData()))
                .flatMap(userRepository::save)
                .thenMany(userRepository.findAll())
                .subscribe(user -> {
                    log.info("User Inserted from CommandLineRunner " + user);
                });
    }
}

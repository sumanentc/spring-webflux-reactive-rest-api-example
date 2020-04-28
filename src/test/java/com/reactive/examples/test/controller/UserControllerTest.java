package com.reactive.examples.test.controller;

import com.reactive.examples.model.User;
import com.reactive.examples.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureWebTestClient
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;


    private List<User> getData(){
        return Arrays.asList(new User("1","Suman Das",30,10000),
                new User("2","Arjun Das",5,1000),
                new User("3","Saurabh Ganguly",40,1000000));
    }

    @BeforeEach
    public  void setup(){
        userRepository.deleteAll()
                .thenMany(Flux.fromIterable(getData()))
                .flatMap(userRepository::save)
                .doOnNext(user ->{
                    System.out.println("User Inserted from UserControllerTest: " + user);
                })
                .blockLast();

    }

    @Test
    public void getAllUsersValidateCount(){
        webTestClient.get().uri("/users").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBodyList(User.class)
                .hasSize(3)
                .consumeWith(user ->{
                    List<User> users = user.getResponseBody();
                    users.forEach( u ->{
                        assertTrue(u.getId() != null);
                    });
                });
    }
    @Test
    public void getAllUsersValidateResponse(){
        Flux<User> userFlux = webTestClient.get().uri("/users").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .returnResult(User.class)
                .getResponseBody();
        StepVerifier.create(userFlux.log("Receiving values !!!"))
                .expectNextCount(3)
                .verifyComplete();

    }
    @Test
    public void getUserById(){
        webTestClient.get().uri("/users".concat("/{userId}"),"1")
                            .exchange().expectStatus().isOk()
                            .expectBody()
                            .jsonPath("$.name","Suman Das");
    }
    @Test
    public void getUserById_NotFound(){
        webTestClient.get().uri("/users".concat("/{userId}"),"6")
                .exchange().expectStatus().isNotFound();
    }
    @Test
    public void createUser(){
        User user = new User(null,"Rahul Dravid",45,5555555);
        webTestClient.post().uri("/users").contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                            .body(Mono.just(user),User.class)
                            .exchange()
                            .expectStatus().isCreated()
                            .expectBody()
                            .jsonPath("$.id").isNotEmpty()
                            .jsonPath("$.name").isEqualTo("Rahul Dravid");
    }
    @Test
    public void deleteUser(){
        webTestClient.delete().uri("/users".concat("/{userId}"),"1")
                     .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                     .exchange()
                     .expectStatus().isOk()
                     .expectBody(Void.class);
    }
    @Test
    public void updateUser(){
        double newsalary = 12345;
        User user = new User(null,"Suman Das",31,newsalary);
        webTestClient.put().uri("/users".concat("/{userId}"),"1")
                        .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                        .body(Mono.just(user),User.class)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.salary").isEqualTo(newsalary);
    }
    @Test
    public void updateUser_notFound(){
        double newsalary = 12345;
        User user = new User(null,"Suman Das",31,newsalary);
        webTestClient.put().uri("/users".concat("/{userId}"),"6")
                .contentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .accept(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE))
                .body(Mono.just(user),User.class)
                .exchange()
                .expectStatus().isBadRequest();
    }
}

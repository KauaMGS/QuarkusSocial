package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.domain.model.Follower;
import br.com.kgsm.quarkussocial.domain.model.Post;
import br.com.kgsm.quarkussocial.domain.model.User;
import br.com.kgsm.quarkussocial.domain.repository.FollowerRepository;
import br.com.kgsm.quarkussocial.domain.repository.PostRepository;
import br.com.kgsm.quarkussocial.domain.repository.UserRepository;
import br.com.kgsm.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        //DEFAULT USER TEST
        User user = new User();
        user.setName("UserTest");
        user.setAge(28);

        userRepository.persist(user);
        userId = user.getId();

        //POST CREATE FOR USER
        Post post = new Post();
        post.setUser(user);
        post.setText("test test test test");

        postRepository.persist(post);

        //NOT FOLLOWER USER
        User userNotFollower = new User();
        userNotFollower.setName("NotFollowerUserTest");
        userNotFollower.setAge(42);

        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //USER FOLLOWER
        User userFollower = new User();
        userFollower.setName("FollowerUserTest");
        userFollower.setAge(40);

        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);

        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("Should create a post for a user")
    public void createPostTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Test text");

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", userId)
        .when()
                .post()
        .then()
                .statusCode(201);

    }

    @Test
    @DisplayName("Should return 404 when try to make a post to a inexistent user")
    public void PostForInexistentUserTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Test text");

        var inexistentUserId = 99;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParam("userId", inexistentUserId)
        .when()
                .post()
        .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSend(){
        given()
                .pathParam("userId", userId)
        .when()
                .get()
        .then()
                .statusCode(400)
                .body(Matchers.is("You forgot the header followerId"));
    }

    @Test
    @DisplayName("Should return 404 when user doesn't exist")
    public void listPostUserNotFound(){
        var inexistentUserId = 99;

        given()
                .pathParam("userId", inexistentUserId)
                .header("followerId", userFollowerId)
        .when()
                .get()
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Should return 400 when follower doesn't exist")
    public void listPostFollowerNotFound(){
        var inexistentFollowerId = 99;

        given()
                .pathParam("userId", userId)
                .header("followerId", inexistentFollowerId)
        .when()
                .get()
        .then()
                .statusCode(400)
                .body(Matchers.is("Inexistent followerId"));
    }

    @Test
    @DisplayName("Should return 403 when follower isn't a follower")
    public void listPostNotAFollower(){
        given()
                .pathParam("userId", userId)
                .header("followerId", userNotFollowerId)
        .when()
                .get()
        .then()
                .statusCode(403)
                .body(Matchers.is("You cant see these posts"));
    }

    @Test
    @DisplayName("Should return posts")
    public void listPostsTest(){
        given()
                .pathParam("userId", userId)
                .header("followerId", userFollowerId)
        .when()
                .get()
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

}
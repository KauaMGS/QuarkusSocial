package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.domain.model.Follower;
import br.com.kgsm.quarkussocial.domain.model.User;
import br.com.kgsm.quarkussocial.domain.repository.FollowerRepository;
import br.com.kgsm.quarkussocial.domain.repository.UserRepository;
import br.com.kgsm.quarkussocial.rest.dto.FollowerRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        //DEFAULT USER TEST
        User user = new User();
        user.setName("UserTest");
        user.setAge(28);

        userRepository.persist(user);
        userId = user.getId();

        //FOLLOWER
        User follower = new User();
        follower.setName("FollowerTest");
        follower.setAge(28);

        userRepository.persist(follower);
        followerId = follower.getId();

        Follower followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("Should return 409 when followerId is equals userId")
    public void sameUserAsFollowerTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can't follow yourself!"));

    }

    @Test
    @DisplayName("Should return 404 when userId doesn't exist")
    public void userTryingToFollowNotFoundTest(){
        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var inexistentUserId = 99;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", inexistentUserId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    @DisplayName("Should follow a user")
    public void followUserTest(){
        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParam("userId", userId)
        .when()
                .put()
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

    @Test
    @DisplayName("Should return 404 on list followers and userId doesn't exist")
    public void userNotFoundWhenListingFollowersTest(){
        var inexistentUserId = 99;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", inexistentUserId)
        .when()
                .get()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    @DisplayName("Should return a list of the followers")
    public void getFollowersTest(){
        var response = given()
                                .contentType(ContentType.JSON)
                                .pathParam("userId", userId)
                        .when()
                                .get()
                        .then()
                                .extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());

    }

    @Test
    @DisplayName("Should return 404 on unfollow user and userId doesn't exist")
    public void userNotFoundWhenUnfollowingAUserTest(){
        var inexistentUserId = 99;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", inexistentUserId)
                .queryParam("followerId", followerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

    }

    @Test
    @DisplayName("Should unfollow a user")
    public void unfollowUserTest(){
        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
        .when()
                .delete()
        .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

    }

}
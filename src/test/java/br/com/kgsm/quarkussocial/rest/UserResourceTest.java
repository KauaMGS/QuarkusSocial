package br.com.kgsm.quarkussocial.rest;

import br.com.kgsm.quarkussocial.rest.dto.CreateUserRequest;
import br.com.kgsm.quarkussocial.rest.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("Should create an user")
    @Order(1)
    public void createUserTest(){
        var user = new CreateUserRequest();
        user.setName("Teste");
        user.setAge(20);

        var response = given()
                .contentType(ContentType.JSON)
                .body(user)
        .when()
                .post(apiURL)
        .then()
                .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));

    }

    @Test
    @DisplayName("Should return an error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest() {
        var user = new CreateUserRequest();
        user.setName(null);
        user.setAge(null);

        var response = given()
                .contentType(ContentType.JSON)
                .body(user)
        .when()
                .post(apiURL)
        .then().extract().response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message")); //Verifica se a mensagem "Age is required" esta sendo retornada
        assertNotNull(errors.get(1).get("message"));//Verifica se a mensagem "Name is required" esta sendo retornada

    }

    @Test
    @DisplayName("Should list all users")
    @Order(3)
    public void listAllUsersTest(){
        given()
                .contentType(ContentType.JSON)
        .when()
                .get(apiURL)
        .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));

    }

}
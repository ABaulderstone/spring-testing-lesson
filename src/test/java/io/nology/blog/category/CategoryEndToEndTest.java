package io.nology.blog.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CategoryEndToEndTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        categoryRepository.deleteAll();

        Category category1 = new Category();
        category1.setName("code");
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setName("art");
        categoryRepository.save(category2);
    }

    @Test
    public void findAllCategories() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("name", hasItems("code", "art"))
                .body(matchesJsonSchemaInClasspath("io/nology/blog/category/schemas/categories-schema.json"));

    }

    @Test
    public void createCategory_success() {
        // set up a dto
        CreateCategoryDTO data = new CreateCategoryDTO();
        data.setName("Test");

        given()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("/categories")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                // remember we transform to lowercase
                .body("name", equalTo("test"))
                .body("id", notNullValue())
                .body(matchesJsonSchemaInClasspath("io/nology/blog/category/schemas/category-schema.json"));
        // we can also check that the category has been saved
        given().contentType(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(3))
                .body("name", hasItems("code", "art", "test"));
    }

    @Test
    public void createCategory_nullName_failure() {
        CreateCategoryDTO data = new CreateCategoryDTO();

        given()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("/categories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void createCategory_emptyName_failure() {
        CreateCategoryDTO data = new CreateCategoryDTO();
        data.setName("");
        given()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("/categories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void createCategory_repeatedName_failure() {
        CreateCategoryDTO data = new CreateCategoryDTO();
        data.setName("art");
        given()
                .contentType(ContentType.JSON)
                .body(data)
                .when()
                .post("/categories")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType(ContentType.JSON)
                .body("errors", notNullValue());
        // .body("errors.name", hasItem("value must be unique"))
        // .body("errors.name.size()", equalTo(1));
    }

}

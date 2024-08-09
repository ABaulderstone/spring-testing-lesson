# Spring Testing

## Why do we test the API layer?

- To ensure that it does everything we think it will
- Because manually checking everything in postman gets annoying
- There's lots of complexity
- To ensure when we're building features that we don't break existing functionality

## How should we test it?

- There are many, many different approaches.
- We will use `Rest Assured` for end to end testing. This will replace the need for manually checking things with postman all the time
- We will use `Mockito` to test the **buisness logic** of our service layer, in isolation from the rest of the application.

## Setup

The first step is adding required dependencies to the pom.xml

```xml
	<!-- Test dependencies -->

		<dependency>
        	<groupId>org.springframework.boot</groupId>
        	<artifactId>spring-boot-starter-test</artifactId>
        	<scope>test</scope>
    	</dependency>

		<dependency>
			<groupId>org.mockito</groupId>
			<artifactId>mockito-core</artifactId>
			<version>5.3.1</version>
			<scope>test</scope>
		</dependency>

    	<dependency>
        	<groupId>io.rest-assured</groupId>
        	<artifactId>rest-assured</artifactId>
        	<scope>test</scope>
    	</dependency>

		<dependency>
        	<groupId>io.rest-assured</groupId>
        	<artifactId>json-path</artifactId>
        	<scope>test</scope>
    	</dependency>

		<dependency>
        	<groupId>io.rest-assured</groupId>
        	<artifactId>json-schema-validator</artifactId>
        	<scope>test</scope>
    	</dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>

```

You might notice the last dependency is a database. We could choose to run our tests against our mysql database, or a different test specific database. However running our tests on a h2 **in memory** database will be faster, and will allow us to have a simpler setup for **continuous integration** later on. There's always a trade off here but this is a good compromise.

We can actually create a seperate `resources` directory in test, to configure it differently to our main environment. Let's create a directory with an `application.properties`

```
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

Note that if we wanted to we could connect to a different mysql db here and have tests that even more accurately mimic our production environment

Just like in our main app it makes sense to split the tests into domain. So let's create a
`category` package within `test` and write our end to end tests

```java
package io.nology.blog.category;

public class CategoryEndToEndTest {

}
```

It's very important to note that our test files **must** end in `Test` or they will not be picked up by the test runner.
We want to make this an EndToEnd or Integration test, this is essentially mimicing real world requests to our API. `RestAssured` allows us to do this but needs a bit of setup

```java
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
        category1.setName("Code");
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setName("Art");
        categoryRepository.save(category2);
    }

}
```

Here we're setting up our test to run on a random port, ensuring it's using the test `application.properties` (and therefore our h2 database). And making sure that we have a clean slate of data ready before each test in this file. This does mean that we create and delete quite a bit of data. But it ensures our tests always run independently of each other.

Now we can start writing out tests. Remember to import given and matchers

```java
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
 @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CategoryEndToEndTest {
    //...setup

    @Test
    public void findAllCategories() {
        given()
                .when()
                .get("/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("name", hasItems("Code", "Art"));

    }
}
```

This test is following BDD (Behaviour Driven Development) test syntax. The idea is that `given()` is where we would set up any additional details of the request. Things like headers, a body, etc. `when()` is where we make the request itselfand `then()` is where we check the results. This test checks that we have an OK status code, that our resulting array is 2 items long, and that we have the right data in the name section of our result.
We can go even further and check that our result matches a defined **schema** thanks to one of the depdencies we added `json-schema-validator`.
We could put our schema in the `category` package

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "array",
  "items": {
    "type": "object",
    "properties": {
      "id": {
        "type": "integer"
      },
      "name": {
        "type": "string"
      }
    },
    "required": ["id", "name"]
  }
}
```

Now we can test that our response matches this schema exactly. Something that will be very useful for us

```java
    import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
    ///... rest of class
    @Test
    public void findAllCategories() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("name", hasItems("Code", "Art"))
                .body(matchesJsonSchemaInClasspath("io/nology/blog/category/schemas/categories-schema.json"));

    }

```

We also want to test our `POST`

```java
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
```

Unlike the `GET` there are some things that could cause our post request to fail. We should have a test for all of them

```java
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
}
```

Right now we're just testing the body, but we'll need to consider the schema. Let's also test the unique constraint on the name.

```java
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
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
```

This test fails, but not for the reason we expect. We're getting a 500 error instead of a 400. We have exposed an edge case that we haven't thought of yet with our code. A great example of why writing automated tests is important. We already have a `GlobalExceptionHandler` and our `ValidationErrors` object. Let's use them to fix this test.

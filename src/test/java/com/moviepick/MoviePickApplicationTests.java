
package com.moviepick;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "JWT_SECRET=test-only-jwt-secret-key-must-be-at-least-32-bytes",
        "TMDB_ACCESS_TOKEN=test-dummy-tmdb-token"
})
class MoviePickApplicationTests {

    @Test
    void contextLoads() {
    }
}
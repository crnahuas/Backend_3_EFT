package cl.duoc.bancoxyz.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:eft-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.batch.job.enabled=false"
})
class BatchServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}


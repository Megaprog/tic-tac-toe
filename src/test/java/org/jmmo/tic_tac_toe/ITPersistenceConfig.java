package org.jmmo.tic_tac_toe;

import org.jmmo.tic_tac_toe.config.CommonConfig;
import org.jmmo.tic_tac_toe.config.PersistenceConfig;
import org.mockito.Mockito;
import org.springframework.context.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;


@Configuration
@PropertySource(value = "classpath:/test.properties", ignoreResourceNotFound = true)
@Import({CommonConfig.class, PersistenceConfig.class})
@ComponentScan(basePackages = {
        "org.jmmo.tic_tac_toe.service"
})
public class ITPersistenceConfig {

    @Bean
    public TestDateSupplier testDateSupplier() {
        return new TestDateSupplier();
    }

    @Bean
    public TestRandomSupplier testRandomSupplier() {
        return new TestRandomSupplier();
    }

    public static class TestDateSupplier implements Supplier<Date> {
        private Date value;

        public TestDateSupplier() {
            this(Date.from(Instant.now()));
        }

        public TestDateSupplier(Date value) {
            this.value = value;
        }

        public void set(Date value) {
            this.value = value;
        }

        @Override
        public Date get() {
            return value;
        }
    }


    public static class TestRandomSupplier implements Supplier<ThreadLocalRandom> {
        private final ThreadLocalRandom mockRandom = Mockito.mock(ThreadLocalRandom.class);

        @Override
        public ThreadLocalRandom get() {
            return mockRandom;
        }
    }

}

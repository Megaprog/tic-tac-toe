package org.jmmo.tic_tac_toe.config;

import org.springframework.context.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@Configuration
@PropertySource("classpath:application.properties")
@PropertySource(value = "file:${ttt.config}", ignoreResourceNotFound = true)
@Import({CommonConfig.class, PersistenceConfig.class})
@ComponentScan(basePackages = {
        "org.jmmo.tic_tac_toe"
})
public class AppConfig {

    @Bean
    public Supplier<Date> dateSupplier() {
        return () -> Date.from(Instant.now());
    }

    @Bean
    public Supplier<ThreadLocalRandom> randomSupplier() {
        return ThreadLocalRandom::current;
    }
}

package org.jmmo.tic_tac_toe.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@PropertySource(value = "file:${ttt.config}", ignoreResourceNotFound = true)
@Import({CommonConfig.class, PersistenceConfig.class})
@ComponentScan(basePackages = {
        "org.jmmo.tic_tac_toe"
})
public class AppConfig {
}

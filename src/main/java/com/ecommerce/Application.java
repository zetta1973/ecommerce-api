package com.ecommerce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Imprimir eventos de arranque
    @EventListener
    public void handleApplicationEvents(ApplicationContextInitializedEvent event) {
        logger.info("ApplicationContext initialized: {}", event.getTimestamp());
    }

    @EventListener
    public void handleApplicationEvents(ContextRefreshedEvent event) {
        logger.info("ApplicationContext refreshed: {}", event.getTimestamp());
    }

    @EventListener
    public void handleApplicationEvents(ApplicationStartedEvent event) {
        logger.info("Application started: {}", event.getTimestamp());
    }

    // Imprimir todos los beans cargados
    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            logger.info("Beans loaded in ApplicationContext:");
            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                logger.info("Bean: {}", beanName);
            }
        };
    }
}

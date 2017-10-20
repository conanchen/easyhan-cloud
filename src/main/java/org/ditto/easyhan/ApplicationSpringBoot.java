package org.ditto.easyhan;

import org.ditto.easyhan.grpc.GreeterService;
import org.ditto.easyhan.grpc.WordService;
import org.ditto.easyhan.service.Publisher;
import org.ditto.easyhan.service.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.Environment;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;

import static reactor.bus.selector.Selectors.$;

@SpringBootApplication
public class ApplicationSpringBoot implements CommandLineRunner {
    private static final int NUMBER_OF_QUOTES = 1;

    @Bean
    public GreeterService greeterService() {
        return new GreeterService();
    }


    @Bean
    public WordService imageService() {
        return new WordService();
    }

    @Bean
    Environment env() {
        return Environment.initializeIfEmpty()
                .assignErrorJournal();
    }

    @Bean
    EventBus createEventBus(Environment env) {
        return EventBus.create(env, Environment.THREAD_POOL);
    }

    @Bean
    public CountDownLatch latch() {
        return new CountDownLatch(NUMBER_OF_QUOTES);
    }

    @Autowired
    private EventBus eventBus;

    @Autowired
    private Receiver receiver;

    @Autowired
    private Publisher publisher;

    @Override
    public void run(String... args) throws Exception {
        eventBus.on($("quotes"), receiver);
        publisher.publishQuotes(NUMBER_OF_QUOTES);
    }

    public static void main(String[] args) {
        SpringApplication.run(ApplicationSpringBoot.class, args);
    }
}

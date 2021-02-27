package gs.app.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.scheduling.annotation.EnableScheduling;

import gs.app.token.service.TokenClientService;

@PropertySources({ @PropertySource("classpath:application.properties"),
    @PropertySource(value = "file:${token_client_config}", ignoreResourceNotFound = true) })
@SpringBootApplication
@EnableScheduling
public class TokenApplication {

    private static Logger LOGGER = LoggerFactory.getLogger(TokenApplication.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication
            .run(TokenApplication.class, args);
        configureClients(applicationContext);
    }

    /**
     * configure monitors based on configuration
     *
     * @param applicationContext
     */
    private static void configureClients(ConfigurableApplicationContext applicationContext) {
        TokenClientService tokenClientService = applicationContext.getBean(TokenClientService.class);
        tokenClientService.loadClientConfiguration();
    }

}

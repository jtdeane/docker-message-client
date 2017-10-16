package ws.cogito.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Simple application for message testing in docker
 * @author jeremydeane
 *
 */
@SpringBootApplication
@ImportResource("classpath:docker-client-spring.xml")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
package ru.advantum.av.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class FilestorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilestorageApplication.class, args);
    }

}

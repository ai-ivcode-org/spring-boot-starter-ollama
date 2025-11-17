package org.ivcode.ai.ollama;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.github.ollama4j.utils.Utils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;

public class Main {
    public static void main(String[] args) {
        Utils.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        var app = new SpringApplication(OllamaCli.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }
}

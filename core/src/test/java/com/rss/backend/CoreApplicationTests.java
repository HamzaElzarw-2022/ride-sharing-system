package com.rss.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.docs.Documenter;
import org.springframework.modulith.core.ApplicationModules;

@SpringBootTest
class CoreApplicationTests {
    ApplicationModules modules = ApplicationModules.of(CoreApplication.class);

    @Test
    void contextLoads() {
    }

    @Test
    void shouldBeCompliant() {
        modules.forEach(System.out::println);
        modules.verify();
    }

    @Test
    void writeDocumentationSnippets() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}

package org.pjp.rosta.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;

import org.junit.jupiter.api.Test;

class MongoConfigTest {

    @Test
    void testGetDatabaseName() {
        MongoConfig config = new MongoConfig();

        assertEquals("test", config.getDatabaseName());
    }

    @Test
    void testGetMappingBasePackages() {
        MongoConfig config = new MongoConfig();

        Collection<String> packages = config.getMappingBasePackages();

        assertThat(packages, hasSize(1));
        assertThat(packages, contains("org.pjp.rosta.model"));
    }

}

package com.myapp.recipe;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import com.opentable.db.postgres.embedded.EmbeddedPostgres;

import java.util.Map;
import java.util.HashMap;

public class EmbeddedPostgresResource implements QuarkusTestResourceLifecycleManager {

    private EmbeddedPostgres postgres;

    @Override
    public Map<String, String> start() {
        try {
            postgres = EmbeddedPostgres.start();
            return Map.of(
                    "quarkus.datasource.jdbc.url",
                    postgres.getJdbcUrl("postgres")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            if (postgres != null) {
                postgres.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
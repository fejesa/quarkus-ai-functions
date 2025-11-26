package io.crunch.ai.function.institute;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class UserSearchTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.otel.enabled", "false");
    }
}

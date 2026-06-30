package com.example.fooddelivery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class SupabaseBuildConfigContractTest {
    @Test
    public void gradleValidatesAndNormalizesSupabaseBuildConfig() throws Exception {
        String gradle = read("app/build.gradle.kts");

        assertTrue(gradle.contains("requiredLocalProperty"));
        assertTrue(gradle.contains("normalizeSupabaseUrl"));
        assertTrue(gradle.contains("javaStringLiteral"));
        assertTrue(gradle.contains("Missing $name in local.properties"));
        assertTrue(gradle.contains("SUPABASE_URL must be a valid http(s) URL"));
        assertTrue(gradle.contains("if (value.endsWith(\"/\")) value else \"$value/\""));
        assertTrue(gradle.contains("buildConfigField(\"String\", \"SUPABASE_URL\", javaStringLiteral(supabaseUrl))"));
        assertTrue(gradle.contains("buildConfigField(\"String\", \"SUPABASE_ANON_KEY\", javaStringLiteral(supabaseAnonKey))"));
    }

    @Test
    public void repositoryProvidesLocalPropertiesExampleWithoutSecrets() throws Exception {
        String example = read("local.properties.example");

        assertTrue(example.contains("SUPABASE_URL=https://YOUR_PROJECT_REF.supabase.co/"));
        assertTrue(example.contains("SUPABASE_ANON_KEY=YOUR_SUPABASE_ANON_KEY"));
    }

    @Test
    public void supabaseClientAddsAuthHeadersAndUsesValidatedBaseUrl() throws Exception {
        String source = read("app/src/main/java/com/example/fooddelivery/data/remote/SupabaseClient.java");

        assertTrue(source.contains("sessionManager.getBearerToken()"));
        assertTrue(source.contains(".addHeader(\"apikey\", Constants.SUPABASE_ANON_KEY)"));
        assertTrue(source.contains(".addHeader(\"Authorization\", authorizationHeader)"));
        assertTrue(source.contains(".baseUrl(Constants.SUPABASE_URL)"));
    }

    private static String read(String relativePath) throws Exception {
        Path base = Path.of(System.getProperty("user.dir"));
        Path direct = base.resolve(relativePath);
        Path fromAppModule = base.getParent() == null ? direct : base.getParent().resolve(relativePath);
        Path path = Files.exists(direct) ? direct : fromAppModule;
        return new String(
                Files.readAllBytes(path),
                StandardCharsets.UTF_8);
    }
}

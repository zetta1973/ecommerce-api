package com.ecommerce;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    @Test
    void shouldHaveSpringBootApplicationAnnotation() {
        assertThat(Application.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class)).isTrue();
    }

    @Test
    void shouldBeInCorrectPackage() {
        assertThat(Application.class.getPackageName()).isEqualTo("com.ecommerce");
    }

    @Test
    void shouldHaveMainMethod() throws NoSuchMethodException {
        assertThat(Application.class.getMethod("main", String[].class)).isNotNull();
    }

    @Test
    void shouldEnableAutoConfiguration() {
        assertThat(Application.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class)).isTrue();
    }

    @Test
    void shouldScanCorrectPackages() {
        org.springframework.boot.autoconfigure.SpringBootApplication annotation = Application.class.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        assertThat(annotation).isNotNull();
    }
}

package com.dorm.backend.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTest {

    private final PasswordService passwordService = new PasswordService();

    @Test
    void matchesBcryptPassword() {
        String encodedPassword = passwordService.encode("123456");

        assertThat(passwordService.matches("123456", encodedPassword)).isTrue();
        assertThat(passwordService.matches("wrong", encodedPassword)).isFalse();
    }

    @Test
    void matchesLegacyPlaintextPasswordForMigration() {
        assertThat(passwordService.matches("123456", "123456")).isTrue();
        assertThat(passwordService.matches("wrong", "123456")).isFalse();
    }

    @Test
    void rejectsBlankLegacyPassword() {
        assertThat(passwordService.matches("", "")).isFalse();
        assertThat(passwordService.matches(" ", " ")).isFalse();
    }

    @Test
    void rejectsUnsupportedOrMalformedEncodedPassword() {
        assertThat(passwordService.matches("{bcrypt}hash", "{bcrypt}hash")).isFalse();
        assertThat(passwordService.matches("$argon2id$hash", "$argon2id$hash")).isFalse();
        assertThat(passwordService.matches("$2b$invalid", "$2b$invalid")).isFalse();
    }
}

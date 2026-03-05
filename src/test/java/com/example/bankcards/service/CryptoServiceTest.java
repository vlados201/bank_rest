package com.example.bankcards.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CryptoServiceTest {
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        ReflectionTestUtils.setField(cryptoService, "secret", "test-secret-key");
        cryptoService.init();
    }

    @Test
    void encryptAndDecrypt_ShouldReturnOriginalValue() {

        String original = "4111111111111111";

        String encrypted = cryptoService.encrypt(original);
        String decrypted = cryptoService.decrypt(encrypted);
        assertThat(encrypted).isNotBlank();
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void encrypt_ShouldProduceDifferentValuesForSameInput() {

        String plain = "4111111111111111";

        String encrypted1 = cryptoService.encrypt(plain);
        String encrypted2 = cryptoService.encrypt(plain);
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void decrypt_WhenDataCorrupted_ShouldThrowException() {

        String corrupted = "invalid-data";

        assertThatThrownBy(() -> cryptoService.decrypt(corrupted))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to decrypt data");
    }

    @Test
    void encrypt_ShouldReturnBase64String() {

        String encrypted = cryptoService.encrypt("1234567890123456");

        assertThat(encrypted)
                .matches("^[A-Za-z0-9+/=]+$");
    }

    @Test
    void encryptDecrypt_WithDifferentTexts_ShouldWork() {

        String text1 = "1234567890123456";
        String text2 = "9876543210987654";

        String enc1 = cryptoService.encrypt(text1);
        String enc2 = cryptoService.encrypt(text2);
        String dec1 = cryptoService.decrypt(enc1);
        String dec2 = cryptoService.decrypt(enc2);

        assertThat(dec1).isEqualTo(text1);
        assertThat(dec2).isEqualTo(text2);
    }
}

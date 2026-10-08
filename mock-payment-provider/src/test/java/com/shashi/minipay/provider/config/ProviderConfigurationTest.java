package com.shashi.minipay.provider.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "provider.processing-delay-ms=1000",
        "provider.failure-rate=0.25"
})
class ProviderConfigurationTest {

    @Autowired
    private ProviderConfiguration providerConfiguration;

    @Test
    void configurationPropertiesAreLoaded() {
        assertEquals(1000L, providerConfiguration.getProcessingDelayMs());
        assertEquals(0.25, providerConfiguration.getFailureRate());
    }

    @Test
    void configurationSettersWork() {
        providerConfiguration.setProcessingDelayMs(2000L);
        providerConfiguration.setFailureRate(0.5);

        assertEquals(2000L, providerConfiguration.getProcessingDelayMs());
        assertEquals(0.5, providerConfiguration.getFailureRate());
    }
}

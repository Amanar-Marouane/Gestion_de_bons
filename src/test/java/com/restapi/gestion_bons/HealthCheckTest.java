package com.restapi.gestion_bons;

import com.restapi.gestion_bons.util.AppLogger;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

public class HealthCheckTest {
    @BeforeAll
    static void setup() {
        AppLogger.info("Let's pretend we're setting up something important...");
    }

    @BeforeEach
    void init() {
        AppLogger.info("Oh look, another test. What a surprise.");
    }

    @DisplayName("Single test successful — because of course it is")
    @Test
    void testSingleSuccessTest() {
        assumeTrue(true, "Assuming everything magically works.");
        assertEquals(2 - 1, 1, "Because math still works, thankfully.");
    }

    @Test
    @Disabled("Yeah, we’ll totally get to this one day.")
    void testNotImplemented() {
    }
}

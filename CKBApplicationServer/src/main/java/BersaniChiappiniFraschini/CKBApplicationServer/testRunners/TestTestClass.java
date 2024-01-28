package BersaniChiappiniFraschini.CKBApplicationServer.testRunners;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestTestClass {

    @Test
    public void thisIsAnUnnamedTest() {
        fail();
    }

    @Test
    public void thisIsAnotherUnnamedTest() {
        assertTrue(true);
    }

    @Test
    @DisplayName("This test always fails")
    public void namedFailingTest() {
        fail();
    }

    @Test
    @DisplayName("This test always passes")
    public void namedPassingTest() {
        assertTrue(true);
    }
}

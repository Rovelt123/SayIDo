package app.services;

import app.exceptions.ApiException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordServiceTest {

    @ParameterizedTest(name = "[{index}] {4} - {3}")
    @CsvFileSource(resources = "/password-test-cases.csv", numLinesToSkip = 1)
    void passwordValidationFollowsTheTestCaseTable(String password, boolean accepted, String message, String reason, String story) {

        if (accepted) {
            assertDoesNotThrow(() -> PasswordService.passwordValidation(password));
            return;
        }

        ApiException exception = assertThrows(
                ApiException.class,
                () -> PasswordService.passwordValidation(password)
        );

        assertEquals(message, exception.getMessage());
    }
}

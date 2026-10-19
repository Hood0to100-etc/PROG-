import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Part 1 unit tests, using the test data from the brief.
public class RegistrationTest {

    Login login;

    @BeforeEach
    void setUp() {
        login = new Login();
        login.firstName = "Kyle";
        login.lastName = "Smith";
        login.username = "kyl_1";
        login.password = "Ch&&sec@ke99!";
        login.cellphone = "+27838968976";
    }

    // ---------- assertEquals tests ----------

    @Test
    void usernameCorrectlyFormatted() {
        login.username = "kyl_1";
        assertEquals("Username successfully captured.", login.usernameMessage());
    }

    @Test
    void welcomeMessageUsesFirstAndLastName() {
        assertEquals("Welcome Kyle, Smith it is great to see you again.", login.returnLoginStatus(true));
    }

    @Test
    void usernameIncorrectlyFormatted() {
        login.username = "kyle!!!!!!!";
        assertEquals("Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.",
                login.usernameMessage());
    }

    @Test
    void passwordMeetsComplexityRequirements() {
        login.password = "Ch&&sec@ke99!";
        assertEquals("Password successfully captured.", login.passwordMessage());
    }

    @Test
    void passwordDoesNotMeetComplexityRequirements() {
        login.password = "password";
        assertEquals("Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.",
                login.passwordMessage());
    }

    @Test
    void cellPhoneCorrectlyFormatted() {
        login.cellphone = "+27838968976";
        assertEquals("Cell number successfully captured.", login.cellPhoneMessage());
    }

    @Test
    void cellPhoneIncorrectlyFormatted() {
        login.cellphone = "08966553";
        assertEquals("Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.",
                login.cellPhoneMessage());
    }

    // ---------- assertTrue / assertFalse tests ----------

    @Test
    void loginSuccessful() {
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    void loginFailed() {
        assertFalse(login.loginUser("kyl_1", "wrongPassword1!"));
    }

    @Test
    void failedLoginMessage() {
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus(false));
    }

    @Test
    void usernameCorrectlyFormattedReturnsTrue() {
        login.username = "kyl_1";
        assertTrue(login.checkUserName());
    }

    @Test
    void usernameIncorrectlyFormattedReturnsFalse() {
        login.username = "kyle!!!!!!!";
        assertFalse(login.checkUserName());
    }

    @Test
    void passwordMeetsComplexityReturnsTrue() {
        login.password = "Ch&&sec@ke99!";
        assertTrue(login.checkPasswordComplexity());
    }

    @Test
    void passwordDoesNotMeetComplexityReturnsFalse() {
        login.password = "password";
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void cellPhoneCorrectlyFormattedReturnsTrue() {
        login.cellphone = "+27838968976";
        assertTrue(login.checkCellPhoneNumber());
    }

    @Test
    void cellPhoneIncorrectlyFormattedReturnsFalse() {
        login.cellphone = "08966553";
        assertFalse(login.checkCellPhoneNumber());
    }

    // ---------- registerUser() ----------

    @Test
    void registrationSucceedsWithValidDetails() {
        assertEquals("User has been registered successfully.", login.registerUser());
    }

    @Test
    void registrationReportsBadUsernameFirst() {
        login.username = "kyle!!!!!!!";
        login.password = "password";
        assertEquals(Login.USERNAME_ERROR, login.registerUser());
    }

    @Test
    void registrationReportsBadPassword() {
        login.password = "password";
        assertEquals(Login.PASSWORD_ERROR, login.registerUser());
    }

    // ---------- Edge cases ----------

    @Test
    void usernameWithoutUnderscoreIsRejected() {
        login.username = "kyle1";
        assertFalse(login.checkUserName());
    }

    @Test
    void passwordWithoutCapitalIsRejected() {
        login.password = "ch&&sec@ke99!";
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void passwordWithoutNumberIsRejected() {
        login.password = "Ch&&sec@keabc!";
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void passwordWithoutSpecialCharacterIsRejected() {
        login.password = "Chsecake99";
        assertFalse(login.checkPasswordComplexity());
    }

    @Test
    void cellPhoneWithTooManyDigitsIsRejected() {
        login.cellphone = "+27838968976123";
        assertFalse(login.checkCellPhoneNumber());
    }

    // ---------- Registration class ----------

    @Test
    void registrationClassUsesTheSameRules() {
        Registration r = new Registration();
        r.username = "kyl_1";
        r.password = "Ch&&sec@ke99!";
        r.cellphone = "+27838968976";
        assertTrue(r.checkUsername());
        assertTrue(r.checkPassword());
        assertTrue(r.checkCellphone());
        r.username = "kyle!!!!!!!";
        r.password = "password";
        r.cellphone = "08966553";
        assertFalse(r.checkUsername());
        assertFalse(r.checkPassword());
        assertFalse(r.checkCellphone());
    }
}
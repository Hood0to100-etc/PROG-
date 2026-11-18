// Login class is used to validate registration details,
// register the user and check login details.
class Login {

    // Messages required by the brief.
    static final String USERNAME_SUCCESS = "Username successfully captured.";
    static final String USERNAME_ERROR = "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
    static final String PASSWORD_SUCCESS = "Password successfully captured.";
    static final String PASSWORD_ERROR = "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
    static final String CELL_SUCCESS = "Cell number successfully captured.";
    static final String CELL_ERROR = "Cell number is incorrectly formatted or does not contain an international code; please correct the number and try again.";
    static final String REGISTER_SUCCESS = "User has been registered successfully.";
    static final String LOGIN_FAILED = "Username or password incorrect, please try again.";

    // Regular expression for the cell phone number: a plus sign, the international
    // country code (1 to 3 digits, e.g. 27 for South Africa), then the number itself
    // of no more than ten digits.
    //
    // Reference: the pattern is built from the character classes, quantifiers and
    // anchors described in:
    // Oracle. 2026. Pattern (Java Platform SE documentation). [Online]. Available at:
    // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html
    // [Accessed 17 October 2026].
    static final String CELL_REGEX = "^\\+[0-9]{1,3}[0-9]{1,10}$";

    // These variables store the user's details.
    String firstName;
    String lastName;
    String username;
    String password;
    String cellphone;

    // Ensures the username contains an underscore and is no more than five characters long.
    public boolean checkUserName() {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    // Ensures the password is at least eight characters long and contains
    // a capital letter, a number and a special character.
    public boolean checkPasswordComplexity() {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    // Ensures the cell phone number has the correct length and the international code.
    public boolean checkCellPhoneNumber() {
        return cellphone != null && cellphone.matches(CELL_REGEX);
    }

    // The message for the username check.
    public String usernameMessage() {
        if (checkUserName()) {
            return USERNAME_SUCCESS;
        }
        return USERNAME_ERROR;
    }

    // The message for the password check.
    public String passwordMessage() {
        if (checkPasswordComplexity()) {
            return PASSWORD_SUCCESS;
        }
        return PASSWORD_ERROR;
    }

    // The message for the cell phone number check.
    public String cellPhoneMessage() {
        if (checkCellPhoneNumber()) {
            return CELL_SUCCESS;
        }
        return CELL_ERROR;
    }

    // Returns the registration message:
    // the username is wrong, the password is wrong, or the user was registered.
    public String registerUser() {
        if (!checkUserName()) {
            return USERNAME_ERROR;
        } else if (!checkPasswordComplexity()) {
            return PASSWORD_ERROR;
        } else {
            return REGISTER_SUCCESS;
        }
    }

    // Compares the entered login details with the details stored when registering.
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username != null && password != null
                && username.equals(enteredUsername)
                && password.equals(enteredPassword);
    }

    // The message for a successful or failed login.
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + firstName + ", " + lastName + " it is great to see you again.";
        }
        return LOGIN_FAILED;
    }
}
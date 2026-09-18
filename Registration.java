// Registration class stores the user's personal details
// and checks if the username, password and cellphone number are valid.
public class Registration {

    // These variables store the user's registration details.
    String firstName;
    String lastName;
    String username;
    String password;
    String cellphone;

    // These boolean variables store whether each detail is valid or not.
    boolean usernameValid;
    boolean passwordValid;
    boolean cellphoneValid;

    // This method checks if the username is exactly 5 characters
    // and contains an underscore.
    public boolean checkUsername() {

        if (username.length() == 5 && username.contains("_")) {
            usernameValid = true;
        } else {
            usernameValid = false;
        }

        // The method returns true if the username is valid
        // and false if it is not valid.
        return usernameValid;
    }

    // This method checks if the password meets all the required rules.
    // The password must have at least 8 characters,
    // one capital letter, one number and one special character.
    public boolean checkPassword() {

        if (password.length() >= 8

                // Checks for at least one capital letter.
                && password.matches(".*[A-Z].*")

                // Checks for at least one number.
                && password.matches(".*[0-9].*")

                // Checks for at least one special character.
                && password.matches(".*[^a-zA-Z0-9].*")) {

            passwordValid = true;

        } else {
            passwordValid = false;
        }

        // Returns true if the password meets all the requirements.
        return passwordValid;
    }

    // This method checks whether the cellphone number
    // contains an international code and is at least 10 characters long.
    public boolean checkCellphone() {

        if (cellphone.startsWith("+") && cellphone.length() >= 10) {
            cellphoneValid = true;
        } else {
            cellphoneValid = false;
        }

        // Returns true if the cellphone number is valid.
        return cellphoneValid;
    }
}


// Login class is used to validate registration details,
// register the user and check login details.
class Login {

    // These variables store the username, password and cellphone number.
    String username;
    String password;
    String cellphone;

    // This method checks if the username contains an underscore
    // and is no more than 5 characters long.
    public boolean checkUserName() {

        if (username.contains("_") && username.length() <= 5) {
            return true;
        } else {
            return false;
        }
    }

    // This method checks if the password meets the complexity requirements.
    public boolean checkPasswordComplexity() {

        if (password.length() >= 8

                // Checks for a capital letter.
                && password.matches(".*[A-Z].*")

                // Checks for a number.
                && password.matches(".*[0-9].*")

                // Checks for a special character.
                && password.matches(".*[^a-zA-Z0-9].*")) {

            return true;

        } else {
            return false;
        }
    }

    // This method checks if the cellphone number starts with
    // an international code and is at least 10 characters long.
    public boolean checkCellPhoneNumber() {

        if (cellphone.startsWith("+") && cellphone.length() >= 10) {
            return true;
        } else {
            return false;
        }
    }

    // This method returns a message to tell the user
    // whether their registration details are valid.
    public String registerUser() {

        // Checks the username first.
        if (!checkUserName()) {

            return "Username is incorrectly formatted.";

        // If the username is valid, it checks the password.
        } else if (!checkPasswordComplexity()) {

            return "Password does not meet the complexity requirements.";

        // If both checks are successful, registration is successful.
        } else {

            return "User has been registered successfully.";
        }
    }

    // This method compares the username and password entered during login
    // with the username and password stored during registration.
    public boolean loginUser(String enteredUsername, String enteredPassword) {

        if (username.equals(enteredUsername)
                && password.equals(enteredPassword)) {

            return true;

        } else {

            return false;
        }
    }

    // This method gives the user a message depending on
    // whether the login was successful or not.
    public String returnLoginStatus(boolean loginSuccessful) {

        if (loginSuccessful) {

            return "Welcome, it is great to see you again.";

        } else {

            return "Username or password incorrect, please try again.";
        }
    }

    // Main class is used to test if the application is working correctly.
    class Main {

        public static void main(String[] args) {

            // Creates a Registration object.
            Registration registration = new Registration();

            // Adds test registration details.
            registration.username = "kyl_1";
            registration.password = "Ch&&sec@ke99";
            registration.cellphone = "+27838968976";

            // Tests the username, password and cellphone methods.
            System.out.println(registration.checkUsername());
            System.out.println(registration.checkPassword());
            System.out.println(registration.checkCellphone());

            // Creates a Login object.
            Login login = new Login();

            // Stores the username and password for login testing.
            login.username = "kyl_1";
            login.password = "Ch&&sec@ke99";

            // Tests whether the user can be registered.
            System.out.println(login.registerUser());

            // Tests the login using an incorrect username and password.
            // This should return false because the details do not match.
            boolean result = login.loginUser("abc_d", "Password1!");

            // Displays the login status message.
            System.out.println(login.returnLoginStatus(result));
        }
    }
}
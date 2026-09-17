public class Registration {

    String firstName;
    String lastName;
    String username;
    String password;
    String cellphone;

    boolean usernameValid;
    boolean passwordValid;
    boolean cellphoneValid;

    public boolean checkUsername() {

        if (username.length() == 5 && username.contains("_")) {
            usernameValid = true;
        } else {
            usernameValid = false;
        }

        return usernameValid;
    }

    public boolean checkPassword() {

        if (password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*")) {

            passwordValid = true;

        } else {
            passwordValid = false;
        }

        return passwordValid;
    }

    public boolean checkCellphone() {

        if (cellphone.startsWith("+") && cellphone.length() >= 10) {
            cellphoneValid = true;
        } else {
            cellphoneValid = false;
        }

        return cellphoneValid;
    }
}


// Login class
class Login {

    String username;
    String password;
    String cellphone;

    public boolean checkUserName() {

        if (username.contains("_") && username.length() <= 5) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkPasswordComplexity() {

        if (password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*")) {

            return true;

        } else {
            return false;
        }
    }

    public boolean checkCellPhoneNumber() {

        if (cellphone.startsWith("+") && cellphone.length() >= 10) {
            return true;
        } else {
            return false;
        }
    }

    public String registerUser() {

        if (!checkUserName()) {

            return "Username is incorrectly formatted.";

        } else if (!checkPasswordComplexity()) {

            return "Password does not meet the complexity requirements.";

        } else {

            return "User has been registered successfully.";
        }
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {

        if (username.equals(enteredUsername)
                && password.equals(enteredPassword)) {

            return true;

        } else {

            return false;
        }
    }

    public String returnLoginStatus(boolean loginSuccessful) {

        if (loginSuccessful) {

            return "Welcome, it is great to see you again.";

        } else {

            return "Username or password incorrect, please try again.";
        }
    }

class Main {

    public static void main(String[] args) {

        Registration registration = new Registration();

        registration.username = "abc_d";
        registration.password = "Password1!";
        registration.cellphone = "+27821234567";

        System.out.println(registration.checkUsername());
        System.out.println(registration.checkPassword());
        System.out.println(registration.checkCellphone());

        Login login = new Login();

        login.username = "abcd";
        login.password = "Password!";

        System.out.println(login.registerUser());

        boolean result = login.loginUser("abc_d", "Password1!");

        System.out.println(login.returnLoginStatus(result));
        }
    }    
}
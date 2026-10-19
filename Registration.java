import java.util.Scanner;

// Registration class stores the user's personal details.
// The checks are done by the Login class so that the rules are only written once.
public class Registration {

    String firstName;
    String lastName;
    String username;
    String password;
    String cellphone;

    // Copies the registration details into a Login object.
    private Login toLogin() {
        Login login = new Login();
        login.firstName = firstName;
        login.lastName = lastName;
        login.username = username;
        login.password = password;
        login.cellphone = cellphone;
        return login;
    }

    public boolean checkUsername() {
        return toLogin().checkUserName();
    }

    public boolean checkPassword() {
        return toLogin().checkPasswordComplexity();
    }

    public boolean checkCellphone() {
        return toLogin().checkCellPhoneNumber();
    }
}


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


class Main {

    // Prints a question and returns what the user types.
    static String ask(Scanner scanner, String question) {
        System.out.println(question);
        return scanner.nextLine().trim();
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Login login = new Login();

        // ---------- Registration ----------
        System.out.println("=== Registration ===");

        login.firstName = ask(scanner, "Enter your first name:");
        login.lastName = ask(scanner, "Enter your last name:");

        do {
            login.username = ask(scanner, "Enter a username (an underscore and no more than five characters):");
            System.out.println(login.usernameMessage());
        } while (!login.checkUserName());

        do {
            login.password = ask(scanner, "Enter a password:");
            System.out.println(login.passwordMessage());
        } while (!login.checkPasswordComplexity());

        do {
            login.cellphone = ask(scanner, "Enter your cell phone number with the international code (e.g. +27838968976):");
            System.out.println(login.cellPhoneMessage());
        } while (!login.checkCellPhoneNumber());

        System.out.println(login.registerUser());

        // ---------- Login ----------
        System.out.println();
        System.out.println("=== Login ===");

        boolean loggedIn = false;
        while (!loggedIn) {
            String enteredUsername = ask(scanner, "Enter your username:");
            String enteredPassword = ask(scanner, "Enter your password:");
            loggedIn = login.loginUser(enteredUsername, enteredPassword);
            System.out.println(login.returnLoginStatus(loggedIn));
        }

        // ---------- QuickChat (only reached after a successful login) ----------
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        // The user decides how many messages they want to enter.
        int numberOfMessages = 0;
        while (numberOfMessages <= 0) {
            String input = ask(scanner, "How many messages would you like to enter?");
            try {
                numberOfMessages = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                numberOfMessages = 0;
            }
            if (numberOfMessages <= 0) {
                System.out.println("Please enter a whole number greater than 0.");
            }
        }

        int messagesHandled = 0;   // messages that were sent or stored
        int option = 0;

        while (option != 3) {

            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");

            String choice = scanner.nextLine().trim();
            try {
                option = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                option = 0;
            }

            if (option == 1) {

                if (messagesHandled >= numberOfMessages) {
                    System.out.println("You have already entered all " + numberOfMessages + " messages.");
                }

                boolean enteredAny = false;

                // Keep asking for messages until the set number has been reached.
                while (messagesHandled < numberOfMessages) {

                    enteredAny = true;
                    System.out.println();
                    System.out.println("Message " + (messagesHandled + 1) + " of " + numberOfMessages);

                    // Recipient
                    String recipient;
                    String cellResult;
                    do {
                        recipient = ask(scanner, "Enter the recipient's cell number (e.g. +27718693002):");
                        cellResult = Message.checkRecipientCell(recipient);
                        System.out.println(cellResult);
                    } while (!cellResult.equals(Message.CELL_SUCCESS));

                    // Message text
                    String text;
                    String lengthResult;
                    do {
                        text = ask(scanner, "Enter your message:");
                        lengthResult = Message.checkMessageLength(text);
                        System.out.println(lengthResult);
                    } while (!lengthResult.equals(Message.MSG_READY));

                    Message message = new Message(Message.returnTotalMessages(), recipient, text);

                    if (!message.checkMessageID()) {
                        System.out.println("The message ID is invalid. Please enter the message again.");
                        continue;
                    }
                    System.out.println(message.messageIdGenerated());

                    // Send, store or disregard the message.
                    String outcome = message.sentMessage(scanner);
                    System.out.println(outcome);

                    if (outcome.equals(Message.MSG_SENT)) {
                        System.out.println(message.getDetails());
                        messagesHandled++;
                    } else if (outcome.equals(Message.MSG_STORED)) {
                        messagesHandled++;
                    }
                    // A deleted message does not use up one of the messages.
                }

                // Show the total once all the messages have been sent.
                if (enteredAny) {
                    System.out.println();
                    System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
                }

            } else if (option == 2) {

                System.out.println("Coming Soon.");

            } else if (option == 3) {

                System.out.println("Goodbye!");

            } else {

                System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }

        scanner.close();
    }
}
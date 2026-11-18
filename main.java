import java.util.List;
import java.util.Scanner;

// Main runs the whole application: registration, login, then QuickChat.
class main {

    // Prints a question and returns what the user types.
    static String ask(Scanner scanner, String question) {
        System.out.println(question);
        return scanner.nextLine().trim();
    }

    // Reads a whole number; returns -1 if the user did not type one.
    static int readNumber(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Login login = registerUser(scanner);
        logIn(scanner, login);
        runQuickChat(scanner, login);

        scanner.close();
    }

    // ---------- Part 1: registration and login ----------

    // Asks for the user's details and repeats each question until the answer is valid.
    static Login registerUser(Scanner scanner) {

        System.out.println("=== Registration ===");
        Login login = new Login();

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
        return login;
    }

    // Keeps asking for the username and password until they are correct.
    static void logIn(Scanner scanner, Login login) {

        System.out.println();
        System.out.println("=== Login ===");

        boolean loggedIn = false;
        while (!loggedIn) {
            String enteredUsername = ask(scanner, "Enter your username:");
            String enteredPassword = ask(scanner, "Enter your password:");
            loggedIn = login.loginUser(enteredUsername, enteredPassword);
            System.out.println(login.returnLoginStatus(loggedIn));
        }
    }

    // ---------- Part 2: QuickChat (only reached after a successful login) ----------

    static void runQuickChat(Scanner scanner, Login login) {

        System.out.println();
        System.out.println("Welcome to QuickChat.");

        MessageStore store = Message.getStore();
        store.setSender(login.firstName + " " + login.lastName);

        // Part 3: read the stored messages from the JSON file into the arrays.
        int loaded = store.loadStoredFromJson(Message.storeFile);
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from " + Message.storeFile + ".");
        }

        // The user decides how many messages they want to enter.
        int numberOfMessages = 0;
        while (numberOfMessages <= 0) {
            numberOfMessages = readNumber(ask(scanner, "How many messages would you like to enter?"));
            if (numberOfMessages <= 0) {
                System.out.println("Please enter a whole number greater than 0.");
            }
        }

        int messagesEntered = 0;   // messages that were sent or stored
        int option = 0;

        // The application runs until the user chooses to quit.
        while (option != 3) {

            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");

            option = readNumber(scanner.nextLine());

            if (option == 1) {

                if (messagesEntered >= numberOfMessages) {
                    System.out.println("You have already entered all " + numberOfMessages + " messages.");
                } else {
                    messagesEntered = sendMessages(scanner, messagesEntered, numberOfMessages);
                    System.out.println();
                    System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
                }

            } else if (option == 2) {

                System.out.println(Message.printMessages());

            } else if (option == 3) {

                System.out.println("Goodbye!");

            } else if (option == 4) {

                storedMessagesMenu(scanner, store);

            } else {

                System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // Lets the user enter the messages from "start" up to "total" using a for loop.
    // The loop counter is the message number. A message that is deleted does not
    // use up one of the messages, so the counter goes back by one.
    // Returns the number of messages that have been entered.
    static int sendMessages(Scanner scanner, int start, int total) {

        for (int i = start; i < total; i++) {

            System.out.println();
            System.out.println("Message " + (i + 1) + " of " + total);

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

            Message message = new Message(i, recipient, text);

            if (!message.checkMessageID()) {
                System.out.println("The message ID is invalid. Please enter the message again.");
                i--;
                continue;
            }
            System.out.println(message.messageIdGenerated());

            // Send, store or disregard the message.
            String outcome = message.sentMessage(scanner);
            System.out.println(outcome);

            if (outcome.equals(Message.MSG_SENT)) {
                System.out.println(message.getDetails());
            } else if (outcome.equals(Message.MSG_DELETED)) {
                i--;
            }
        }

        return total;
    }

    // ---------- Part 3: Stored Messages ----------

    static void storedMessagesMenu(Scanner scanner, MessageStore store) {

        int choice = 0;

        while (choice != 7) {

            System.out.println();
            System.out.println("=== Stored Messages ===");
            System.out.println("1) Display the sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all the messages for a particular recipient");
            System.out.println("5) Delete a message using the message hash");
            System.out.println("6) Display a report of all the stored messages");
            System.out.println("7) Back to the main menu");

            choice = readNumber(scanner.nextLine());

            if (choice == 1) {

                System.out.println(store.displaySenderAndRecipient());

            } else if (choice == 2) {

                System.out.println(store.longestStoredMessage());

            } else if (choice == 3) {

                String id = ask(scanner, "Enter the message ID:");
                System.out.println(store.searchByMessageId(id));

            } else if (choice == 4) {

                String recipient = ask(scanner, "Enter the recipient's cell number:");
                List<String> found = store.searchByRecipient(recipient);
                if (found.isEmpty()) {
                    System.out.println("No messages were found for " + recipient + ".");
                } else {
                    for (String text : found) {
                        System.out.println("Message: " + text);
                    }
                }

            } else if (choice == 5) {

                String hash = ask(scanner, "Enter the message hash:");
                String result = store.deleteByHash(hash);
                System.out.println(result);
                // Keep the JSON file the same as the array.
                store.saveStoredToJson(Message.storeFile);

            } else if (choice == 6) {

                System.out.println(store.report(MessageStore.STORED));

            } else if (choice == 7) {

                System.out.println("Returning to the main menu.");

            } else {

                System.out.println("Invalid option. Please choose a number from 1 to 7.");
            }
        }
    }
}
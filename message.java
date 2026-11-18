import java.util.Random;
import java.util.Scanner;

// Message class stores the details of one message and does the checks and actions
// for it (send, store or disregard). The arrays that hold all the messages are in
// the MessageStore, which is shared by the whole program.
class Message {

    // The arrays that hold every message while the program is running.
    private static final MessageStore store = new MessageStore();

    // The JSON file that stored messages are written to.
    static String storeFile = "storedMessages.json";

    // Same cell phone rule as Login.CELL_REGEX: a plus sign, the international
    // country code (1 to 3 digits), then the number itself of no more than ten digits.
    // Reference: Oracle. 2026. Pattern (Java Platform SE documentation). [Online].
    // Available at:
    // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/regex/Pattern.html
    // [Accessed 17 October 2026].
    static final String CELL_REGEX = "^\\+[0-9]{1,3}[0-9]{1,10}$";

    // Text returned by the methods below.
    static final String CELL_SUCCESS = "Cell phone number successfully captured.";
    static final String CELL_ERROR = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    static final String MSG_READY = "Message ready to send.";
    static final String MSG_EMPTY = "Message cannot be empty.";
    static final String MSG_SENT = "Message successfully sent.";
    static final String MSG_PRESS_ZERO = "Press 0 to delete the message.";
    static final String MSG_STORED = "Message successfully stored.";
    static final String MSG_STORE_FAILED = "Message could not be stored.";
    static final String MSG_DELETED = "Message deleted.";
    static final String MSG_INVALID_OPTION = "Invalid option. Please choose 1, 2 or 3.";

    private final String messageId;      // random ten-digit number
    private final int numMessagesSent;   // the loop counter: starts at 0, goes up with each message
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    // Creates a message with an automatically generated message ID.
    public Message(int numMessagesSent, String recipient, String messageText) {
        this(generateMessageId(), numMessagesSent, recipient, messageText);
    }

    // Creates a message with a given message ID (used by the unit tests so that
    // the hash can be predicted).
    public Message(String messageId, int numMessagesSent, String recipient, String messageText) {
        this.messageId = messageId;
        this.numMessagesSent = numMessagesSent;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
    }

    // The arrays that hold all the messages.
    public static MessageStore getStore() {
        return store;
    }

    // Generates a random ten-digit number, one digit at a time.
    private static String generateMessageId() {
        Random random = new Random();
        String id = "";
        for (int i = 0; i < 10; i++) {
            id = id + random.nextInt(10);
        }
        return id;
    }

    // Ensures that the message ID is not more than ten characters.
    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= 10;
    }

    // Returns the message ID in the wording the brief asks for.
    public String messageIdGenerated() {
        return "Message ID generated: " + messageId;
    }

    // Ensures that the recipient number starts with an international code and has
    // no more than ten digits after it.
    // Returns a message saying whether the number was accepted.
    public static String checkRecipientCell(String cell) {
        if (cell != null && cell.matches(CELL_REGEX)) {
            return CELL_SUCCESS;
        }
        return CELL_ERROR;
    }

    // Checks the recipient number stored in this message.
    public String checkRecipientCell() {
        return checkRecipientCell(recipient);
    }

    // Checks that the message is not more than 250 characters.
    // Returns "Message ready to send." or by how many characters it is too long.
    public static String checkMessageLength(String text) {
        if (text == null || text.trim().length() == 0) {
            return MSG_EMPTY;
        }
        if (text.length() <= 250) {
            return MSG_READY;
        }
        int extra = text.length() - 250;
        return "Message exceeds 250 characters by " + extra + "; please reduce the size.";
    }

    // Hash = first two digits of ID : message number : FIRSTWORD + LASTWORD (all caps)
    // Punctuation is removed from the words, e.g. 00:0:HITONIGHT
    public String createMessageHash() {
        String firstWord = "";
        String lastWord = "";
        for (String word : messageText.trim().split("\\s+")) {
            String cleaned = word.replaceAll("[^A-Za-z0-9]", "");
            if (cleaned.length() > 0) {
                if (firstWord.isEmpty()) {
                    firstWord = cleaned;
                }
                lastWord = cleaned;
            }
        }
        String hash = messageId.substring(0, 2) + ":" + numMessagesSent + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }

    // Does what the user chose: 1 = send, 2 = disregard, 3 = store.
    // Returns the message the brief asks for.
    public String sentMessage(int choice) {
        if (choice == 1) {
            store.addMessage(this, MessageStore.SENT);
            return MSG_SENT;
        } else if (choice == 2) {
            return MSG_PRESS_ZERO;
        } else if (choice == 3) {
            if (storeMessage()) {
                return MSG_STORED;
            }
            return MSG_STORE_FAILED;
        }
        return MSG_INVALID_OPTION;
    }

    // Lets the user choose to send, store or disregard this message.
    // Returns text describing what happened to the message.
    public String sentMessage(Scanner scanner) {

        while (true) {

            System.out.println();
            System.out.println("What would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");

            String action = scanner.nextLine().trim();

            if (action.equals("1") || action.equals("3")) {

                String result = sentMessage(Integer.parseInt(action));
                if (result.equals(MSG_SENT) || result.equals(MSG_STORED)) {
                    return result;
                }
                System.out.println(result);

            } else if (action.equals("2")) {

                System.out.println(sentMessage(2));
                String confirm = scanner.nextLine().trim();
                if (confirm.equals("0")) {
                    store.addMessage(this, MessageStore.DISREGARDED);
                    return MSG_DELETED;
                }
                System.out.println("Message was not deleted.");

            } else {

                System.out.println(MSG_INVALID_OPTION);
            }
        }
    }

    // Returns all the messages sent while the program is running.
    public static String printMessages() {
        return store.report(MessageStore.SENT);
    }

    // Returns the total number of messages sent.
    public static int returnTotalMessages() {
        return store.totalSent();
    }

    // Stores this message in the JSON file. Returns true if it was saved.
    public boolean storeMessage() {
        store.addMessage(this, MessageStore.STORED);
        if (store.saveStoredToJson(storeFile)) {
            return true;
        }
        store.deleteByHash(messageHash);
        return false;
    }

    // Clears every array (used by the tests).
    public static void resetMessages() {
        store.clear();
    }

    // The details of this message in the required order:
    // Message ID, Message Hash, Recipient, Message.
    public String getDetails() {
        return "Message ID: " + messageId + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    public String getMessageId() { return messageId; }
    public int getNumMessagesSent() { return numMessagesSent; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
}
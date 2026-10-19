import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Message class stores the details of one message (Part 2).
class Message {

    // Messages sent and stored while the program is running.
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();

    // The JSON file that stored messages are written to.
    static String storeFile = "storedMessages.json";

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
    private final int numMessagesSent;   // starts at 0, goes up as messages are sent
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

    // Generates a random ten-digit number.
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
    // no more than ten digits after it. This reuses the rule from the Login class
    // (Part 1). Returns a message saying whether the number was accepted.
    public static String checkRecipientCell(String cell) {
        if (cell != null && cell.matches(Login.CELL_REGEX)) {
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
        List<String> words = new ArrayList<>();
        for (String word : messageText.trim().split("\\s+")) {
            String cleaned = word.replaceAll("[^A-Za-z0-9]", "");
            if (cleaned.length() > 0) {
                words.add(cleaned);
            }
        }
        String firstWord = "";
        String lastWord = "";
        if (!words.isEmpty()) {
            firstWord = words.get(0);
            lastWord = words.get(words.size() - 1);
        }
        String hash = messageId.substring(0, 2) + ":" + numMessagesSent + ":" + firstWord + lastWord;
        return hash.toUpperCase();
    }

    // Does what the user chose: 1 = send, 2 = disregard, 3 = store.
    // Returns the message the brief asks for.
    public String sentMessage(int choice) {
        if (choice == 1) {
            sentMessages.add(this);
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
        if (sentMessages.isEmpty()) {
            return "No messages have been sent yet.";
        }
        String all = "";
        for (int i = 0; i < sentMessages.size(); i++) {
            all = all + sentMessages.get(i).getDetails();
            if (i < sentMessages.size() - 1) {
                all = all + "\n\n";
            }
        }
        return all;
    }

    // Returns the total number of messages sent.
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    // Stores this message in the JSON file. Returns true if it was saved.
    public boolean storeMessage() {
        storedMessages.add(this);
        if (saveToJsonFile(storedMessages, storeFile)) {
            return true;
        }
        storedMessages.remove(this);
        return false;
    }

    // Clears the sent and stored lists (used by the tests).
    public static void resetMessages() {
        sentMessages.clear();
        storedMessages.clear();
    }

    // The details of this message in the required order:
    // Message ID, Message Hash, Recipient, Message.
    public String getDetails() {
        return "Message ID: " + messageId + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    // Escapes characters that are not allowed raw inside a JSON string.
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Converts this message into one JSON object.
    public String toJson() {
        return "  {\n"
                + "    \"messageId\": \"" + escapeJson(messageId) + "\",\n"
                + "    \"messageHash\": \"" + escapeJson(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
                + "    \"message\": \"" + escapeJson(messageText) + "\"\n"
                + "  }";
    }

    // Writes all stored messages to a JSON file as a JSON array.
    private static boolean saveToJsonFile(List<Message> messages, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("[\n");
            for (int i = 0; i < messages.size(); i++) {
                writer.write(messages.get(i).toJson());
                if (i < messages.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            writer.write("]\n");
            return true;
        } catch (IOException e) {
            System.out.println("Could not write to " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public String getMessageId() { return messageId; }
    public int getNumMessagesSent() { return numMessagesSent; }
    public String getRecipient() { return recipient; }
    public String getMessageText() { return messageText; }
    public String getMessageHash() { return messageHash; }
}
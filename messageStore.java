import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// MessageStore keeps every message in parallel arrays and is used by the
// "Stored Messages" menu to search, delete and report on them.
//
// Parallel arrays: the same index in messageIDs, messageHashes, senders, recipients,
// messages and flags describes ONE message. The three category arrays (sentMessages,
// disregardedMessages and storedMessages) hold the text of the messages in each group.
// All the arrays are filled while the program runs; nothing is hard coded.
//
// References (for reading and writing the JSON file):
// Bray, T. (ed.). 2017. RFC 8259: The JavaScript Object Notation (JSON) Data
// Interchange Format. [Online]. Available at: https://www.rfc-editor.org/rfc/rfc8259
// [Accessed 10 November 2026].
// HowToDoInJava. n.d. Java 11 Files.readString(): read a file into a String. [Online].
// Available at: https://howtodoinjava.com/java11/files-readstring-read-file-to-string
// [Accessed 10 November 2026].
class MessageStore {

    // The flag that says what happened to a message.
    static final String SENT = "Sent";
    static final String STORED = "Stored";
    static final String DISREGARDED = "Disregard";

    static final String NO_SENT_MESSAGES = "No messages have been sent yet.";
    static final String NO_STORED_MESSAGES = "No stored messages.";

    // Parallel arrays: one entry per message, in the order the messages were added.
    private final List<String> messageIDs = new ArrayList<>();
    private final List<String> messageHashes = new ArrayList<>();
    private final List<String> senders = new ArrayList<>();
    private final List<String> recipients = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();
    private final List<String> flags = new ArrayList<>();

    // The message text of each group.
    private final List<String> sentMessages = new ArrayList<>();
    private final List<String> disregardedMessages = new ArrayList<>();
    private final List<String> storedMessages = new ArrayList<>();

    // The person using the app, shown as the sender of the messages.
    private String sender = "User";

    public void setSender(String sender) {
        this.sender = sender;
    }

    // Adds a message to the arrays with the flag Sent, Stored or Disregard.
    public void addMessage(Message message, String flag) {
        addEntry(message.getMessageId(), message.getMessageHash(), sender,
                message.getRecipient(), message.getMessageText(), flag);
    }

    // Adds one message to every array it belongs in.
    private void addEntry(String id, String hash, String messageSender,
                          String recipient, String text, String flag) {
        messageIDs.add(id);
        messageHashes.add(hash);
        senders.add(messageSender);
        recipients.add(recipient);
        messages.add(text);
        flags.add(flag);

        if (flag.equals(SENT)) {
            sentMessages.add(text);
        } else if (flag.equals(STORED)) {
            storedMessages.add(text);
        } else if (flag.equals(DISREGARDED)) {
            disregardedMessages.add(text);
        } else {
            throw new IllegalArgumentException("Unknown flag: " + flag);
        }
    }

    // Empties every array.
    public void clear() {
        messageIDs.clear();
        messageHashes.clear();
        senders.clear();
        recipients.clear();
        messages.clear();
        flags.clear();
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        sender = "User";
    }

    // ---------- The arrays ----------

    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public List<String> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public List<String> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    public List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public List<String> getMessageIDs() {
        return new ArrayList<>(messageIDs);
    }

    public int totalSent() {
        return sentMessages.size();
    }

    // ---------- Stored Messages menu ----------

    // a. Display the sender and recipient of all stored messages.
    public String displaySenderAndRecipient() {
        String result = "";
        for (int i = 0; i < flags.size(); i++) {
            if (flags.get(i).equals(STORED)) {
                if (!result.isEmpty()) {
                    result = result + "\n";
                }
                result = result + "Sender: " + senders.get(i) + " | Recipient: " + recipients.get(i);
            }
        }
        if (result.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        return result;
    }

    // b. Display the longest stored message.
    public String longestStoredMessage() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }
        String longest = storedMessages.get(0);
        for (String text : storedMessages) {
            if (text.length() > longest.length()) {
                longest = text;
            }
        }
        return longest;
    }

    // c. Search for a message ID and display the recipient and the message.
    public String searchByMessageId(String id) {
        int index = messageIDs.indexOf(id);
        if (index < 0) {
            return "No message with ID " + id + " was found.";
        }
        return "Recipient: " + recipients.get(index) + "\nMessage: " + messages.get(index);
    }

    // d. Search for all the messages sent or stored for a particular recipient.
    public List<String> searchByRecipient(String recipient) {
        List<String> found = new ArrayList<>();
        for (int i = 0; i < recipients.size(); i++) {
            boolean sentOrStored = flags.get(i).equals(SENT) || flags.get(i).equals(STORED);
            if (sentOrStored && recipients.get(i).equals(recipient)) {
                found.add(messages.get(i));
            }
        }
        return found;
    }

    // e. Delete a message using its message hash. The message is removed from
    // every array so the parallel arrays stay lined up.
    public String deleteByHash(String hash) {
        int index = indexOfHash(hash);
        if (index < 0) {
            return "No message with hash " + hash + " was found.";
        }

        String text = messages.get(index);
        String flag = flags.get(index);

        if (flag.equals(SENT)) {
            sentMessages.remove(text);
        } else if (flag.equals(STORED)) {
            storedMessages.remove(text);
        } else {
            disregardedMessages.remove(text);
        }

        messageIDs.remove(index);
        messageHashes.remove(index);
        senders.remove(index);
        recipients.remove(index);
        messages.remove(index);
        flags.remove(index);

        return "Message: \"" + text + "\" successfully deleted.";
    }

    private int indexOfHash(String hash) {
        for (int i = 0; i < messageHashes.size(); i++) {
            if (messageHashes.get(i).equalsIgnoreCase(hash)) {
                return i;
            }
        }
        return -1;
    }

    // f. Display a report with the full details of all the messages with the given
    // flag (Sent or Stored).
    public String report(String flag) {
        String result = "";
        for (int i = 0; i < flags.size(); i++) {
            if (flags.get(i).equals(flag)) {
                if (!result.isEmpty()) {
                    result = result + "\n\n";
                }
                result = result + "Message ID: " + messageIDs.get(i) + "\n"
                        + "Message Hash: " + messageHashes.get(i) + "\n"
                        + "Sender: " + senders.get(i) + "\n"
                        + "Recipient: " + recipients.get(i) + "\n"
                        + "Message: " + messages.get(i);
            }
        }
        if (result.isEmpty()) {
            if (flag.equals(SENT)) {
                return NO_SENT_MESSAGES;
            }
            return NO_STORED_MESSAGES;
        }
        return result;
    }

    // ---------- JSON file ----------

    // Writes every stored message to the JSON file. Returns true if it was saved.
    public boolean saveStoredToJson(String fileName) {
        List<Map<String, String>> objects = new ArrayList<>();
        for (int i = 0; i < flags.size(); i++) {
            if (flags.get(i).equals(STORED)) {
                Map<String, String> object = new LinkedHashMap<>();
                object.put("messageId", messageIDs.get(i));
                object.put("messageHash", messageHashes.get(i));
                object.put("sender", senders.get(i));
                object.put("recipient", recipients.get(i));
                object.put("message", messages.get(i));
                objects.add(object);
            }
        }
        try {
            Files.writeString(Path.of(fileName), JsonHelper.toJson(objects));
            return true;
        } catch (IOException e) {
            System.out.println("Could not write to " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    // Reads the JSON file into the arrays as stored messages. Returns how many
    // messages were loaded (0 if the file is missing or cannot be read).
    public int loadStoredFromJson(String fileName) {
        Path path = Path.of(fileName);
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            List<Map<String, String>> objects = JsonHelper.parseArray(Files.readString(path));
            int loaded = 0;
            for (Map<String, String> object : objects) {
                String id = object.getOrDefault("messageId", "");
                String hash = object.getOrDefault("messageHash", "");
                if (messageIDs.contains(id) && messageHashes.contains(hash)) {
                    continue;   // already in the arrays
                }
                addEntry(id, hash, object.getOrDefault("sender", sender),
                        object.getOrDefault("recipient", ""),
                        object.getOrDefault("message", ""), STORED);
                loaded++;
            }
            return loaded;
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Could not read " + fileName + ": " + e.getMessage());
            return 0;
        }
    }
}
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Part 3 unit tests for the arrays, the Stored Messages menu features and the JSON file.
// The test data is the five test messages from the brief.
public class MessageStoreTest {

    @TempDir
    Path tempDir;

    // The message texts from the brief.
    static final String TEXT_1 = "Did you get the cake?";
    static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    static final String TEXT_3 = "Yohoooo, I am at your gate.";
    static final String TEXT_4 = "It is dinner time !";
    static final String TEXT_5 = "Ok, I am leaving without you.";

    // The brief gives no recipient for message 4 (only 0838884567, which is used as
    // its message ID in the "Search for messageID" test), so a different number is used.
    static final String RECIPIENT_4 = "+27835550000";

    MessageStore store;

    @BeforeEach
    void setUp() {
        store = new MessageStore();
        store.setSender("Kyle Smith");
    }

    // Adds test messages 1 to 4 (the "message 1-4" test data).
    private void addMessages1To4() {
        store.addMessage(new Message("1100000001", 0, "+27834557896", TEXT_1), MessageStore.SENT);
        store.addMessage(new Message("2200000002", 1, "+27838884567", TEXT_2), MessageStore.STORED);
        store.addMessage(new Message("3300000003", 2, "+27834484567", TEXT_3), MessageStore.DISREGARDED);
        store.addMessage(new Message("0838884567", 3, RECIPIENT_4, TEXT_4), MessageStore.SENT);
    }

    // Adds all five test messages.
    private void addAllMessages() {
        addMessages1To4();
        store.addMessage(new Message("5500000005", 4, "+27838884567", TEXT_5), MessageStore.STORED);
    }

    // ---------- Arrays correctly populated ----------

    @Test
    void arraysStartEmpty() {
        assertTrue(store.getSentMessages().isEmpty());
        assertTrue(store.getDisregardedMessages().isEmpty());
        assertTrue(store.getStoredMessages().isEmpty());
        assertTrue(store.getMessageHashes().isEmpty());
        assertTrue(store.getMessageIDs().isEmpty());
    }

    @Test
    void sentMessagesArrayIsCorrectlyPopulated() {
        addMessages1To4();
        assertEquals(List.of("Did you get the cake?", "It is dinner time !"), store.getSentMessages());
    }

    @Test
    void disregardedMessagesArrayIsCorrectlyPopulated() {
        addMessages1To4();
        assertEquals(List.of("Yohoooo, I am at your gate."), store.getDisregardedMessages());
    }

    @Test
    void storedMessagesArrayIsCorrectlyPopulated() {
        addAllMessages();
        assertEquals(List.of(TEXT_2, TEXT_5), store.getStoredMessages());
    }

    @Test
    void messageHashArrayContainsAllTheHashes() {
        addAllMessages();
        assertEquals(List.of("11:0:DIDCAKE", "22:1:WHERETIME", "33:2:YOHOOOOGATE",
                "08:3:ITTIME", "55:4:OKYOU"), store.getMessageHashes());
    }

    @Test
    void messageIdArrayContainsAllTheIds() {
        addAllMessages();
        assertEquals(List.of("1100000001", "2200000002", "3300000003", "0838884567", "5500000005"),
                store.getMessageIDs());
    }

    @Test
    void totalSentCountsOnlySentMessages() {
        addAllMessages();
        assertEquals(2, store.totalSent());
    }

    @Test
    void clearEmptiesEveryArray() {
        addAllMessages();
        store.clear();
        assertTrue(store.getSentMessages().isEmpty());
        assertTrue(store.getStoredMessages().isEmpty());
        assertTrue(store.getMessageHashes().isEmpty());
        assertTrue(store.getMessageIDs().isEmpty());
    }

    @Test
    void anUnknownFlagIsRejected() {
        boolean rejected = false;
        try {
            store.addMessage(new Message("1100000001", 0, "+27834557896", TEXT_1), "Lost");
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        assertTrue(rejected);
    }

    // ---------- Display the sender and recipient of all stored messages ----------

    @Test
    void displaySenderAndRecipientOfStoredMessages() {
        addAllMessages();
        assertEquals("Sender: Kyle Smith | Recipient: +27838884567\n"
                + "Sender: Kyle Smith | Recipient: +27838884567", store.displaySenderAndRecipient());
    }

    @Test
    void displaySenderAndRecipientWhenNothingIsStored() {
        assertEquals("No stored messages.", store.displaySenderAndRecipient());
    }

    // ---------- Display the longest message ----------

    @Test
    void longestMessageForTestData1To4() {
        addMessages1To4();
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                store.longestStoredMessage());
    }

    @Test
    void longestMessageForAllTheTestData() {
        addAllMessages();
        assertEquals(TEXT_2, store.longestStoredMessage());
    }

    @Test
    void longestMessageWhenNothingIsStored() {
        assertEquals("No stored messages.", store.longestStoredMessage());
    }

    // ---------- Search for a message ID ----------

    @Test
    void searchForMessageIdOfTestMessage4() {
        addMessages1To4();
        String result = store.searchByMessageId("0838884567");
        assertEquals("Recipient: +27835550000\nMessage: It is dinner time !", result);
        assertTrue(result.contains("It is dinner time !"));
    }

    @Test
    void searchForAnIdThatDoesNotExist() {
        addMessages1To4();
        assertEquals("No message with ID 9999999999 was found.", store.searchByMessageId("9999999999"));
    }

    // ---------- Search all the messages sent or stored for a recipient ----------

    @Test
    void searchForTheMessagesOfOneRecipient() {
        addAllMessages();
        assertEquals(List.of("Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."), store.searchByRecipient("+27838884567"));
    }

    @Test
    void searchIncludesSentMessages() {
        addAllMessages();
        assertEquals(List.of(TEXT_1), store.searchByRecipient("+27834557896"));
    }

    @Test
    void searchDoesNotIncludeDisregardedMessages() {
        addAllMessages();
        assertTrue(store.searchByRecipient("+27834484567").isEmpty());
    }

    @Test
    void searchForARecipientWithNoMessages() {
        addAllMessages();
        assertTrue(store.searchByRecipient("+27800000000").isEmpty());
    }

    // ---------- Delete a message using the message hash ----------

    @Test
    void deleteTestMessage2UsingItsHash() {
        addAllMessages();
        String result = store.deleteByHash("22:1:WHERETIME");
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" successfully deleted.",
                result);
    }

    @Test
    void deletedMessageIsRemovedFromEveryArray() {
        addAllMessages();
        store.deleteByHash("22:1:WHERETIME");

        assertEquals(List.of(TEXT_5), store.getStoredMessages());
        assertEquals(4, store.getMessageHashes().size());
        assertEquals(4, store.getMessageIDs().size());
        assertFalse(store.getMessageHashes().contains("22:1:WHERETIME"));
        assertFalse(store.getMessageIDs().contains("2200000002"));
        assertTrue(store.searchByRecipient("+27838884567").size() == 1);
    }

    @Test
    void theParallelArraysStayLinedUpAfterADelete() {
        addAllMessages();
        store.deleteByHash("22:1:WHERETIME");

        List<String> hashes = store.getMessageHashes();
        List<String> ids = store.getMessageIDs();
        assertEquals("5500000005", ids.get(hashes.indexOf("55:4:OKYOU")));
        assertEquals("0838884567", ids.get(hashes.indexOf("08:3:ITTIME")));
        assertEquals("Recipient: +27838884567\nMessage: " + TEXT_5, store.searchByMessageId("5500000005"));
    }

    @Test
    void deleteWorksWithLowerCaseHash() {
        addAllMessages();
        assertEquals("Message: \"" + TEXT_5 + "\" successfully deleted.", store.deleteByHash("55:4:okyou"));
    }

    @Test
    void deleteWithAnUnknownHash() {
        addAllMessages();
        assertEquals("No message with hash ZZ:9:NONE was found.", store.deleteByHash("ZZ:9:NONE"));
        assertEquals(5, store.getMessageHashes().size());
    }

    @Test
    void deletingASentMessageRemovesItFromTheSentArray() {
        addAllMessages();
        store.deleteByHash("11:0:DIDCAKE");
        assertEquals(List.of(TEXT_4), store.getSentMessages());
    }

    // ---------- Display report ----------

    @Test
    void sentReportShowsHashRecipientAndMessage() {
        addMessages1To4();
        String report = store.report(MessageStore.SENT);

        assertTrue(report.contains("Message Hash: 11:0:DIDCAKE"));
        assertTrue(report.contains("Recipient: +27834557896"));
        assertTrue(report.contains("Message: Did you get the cake?"));
        assertTrue(report.contains("Message Hash: 08:3:ITTIME"));
        assertTrue(report.contains("Recipient: +27835550000"));
        assertTrue(report.contains("Message: It is dinner time !"));
        assertFalse(report.contains("Where are you?"));
        assertTrue(report.indexOf("DIDCAKE") < report.indexOf("ITTIME"));
    }

    @Test
    void storedReportListsTheFullDetailsOfEveryStoredMessage() {
        addAllMessages();
        assertEquals("Message ID: 2200000002\n"
                + "Message Hash: 22:1:WHERETIME\n"
                + "Sender: Kyle Smith\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT_2 + "\n\n"
                + "Message ID: 5500000005\n"
                + "Message Hash: 55:4:OKYOU\n"
                + "Sender: Kyle Smith\n"
                + "Recipient: +27838884567\n"
                + "Message: " + TEXT_5, store.report(MessageStore.STORED));
    }

    @Test
    void reportsWhenThereAreNoMessages() {
        assertEquals("No messages have been sent yet.", store.report(MessageStore.SENT));
        assertEquals("No stored messages.", store.report(MessageStore.STORED));
    }

    // ---------- Reading the JSON file into the arrays ----------

    @Test
    void storedMessagesAreWrittenToAndReadBackFromTheJsonFile() {
        addAllMessages();
        String file = tempDir.resolve("stored.json").toString();
        assertTrue(store.saveStoredToJson(file));

        MessageStore reloaded = new MessageStore();
        assertEquals(2, reloaded.loadStoredFromJson(file));

        assertEquals(List.of(TEXT_2, TEXT_5), reloaded.getStoredMessages());
        assertEquals(List.of("22:1:WHERETIME", "55:4:OKYOU"), reloaded.getMessageHashes());
        assertEquals(List.of("2200000002", "5500000005"), reloaded.getMessageIDs());
        assertEquals("Sender: Kyle Smith | Recipient: +27838884567\n"
                + "Sender: Kyle Smith | Recipient: +27838884567", reloaded.displaySenderAndRecipient());
    }

    @Test
    void onlyStoredMessagesAreWrittenToTheFile() throws IOException {
        addAllMessages();
        String file = tempDir.resolve("stored.json").toString();
        store.saveStoredToJson(file);

        String json = Files.readString(Path.of(file));
        assertTrue(json.contains("Where are you?"));
        assertFalse(json.contains("cake"));
        assertFalse(json.contains("gate"));
    }

    @Test
    void messagesWithQuotesAndNewLinesSurviveTheJsonFile() {
        String tricky = "She said \"hi\"\nand left \\ quickly";
        store.addMessage(new Message("1100000001", 0, "+27834557896", tricky), MessageStore.STORED);
        String file = tempDir.resolve("tricky.json").toString();
        store.saveStoredToJson(file);

        MessageStore reloaded = new MessageStore();
        reloaded.loadStoredFromJson(file);
        assertEquals(List.of(tricky), reloaded.getStoredMessages());
    }

    @Test
    void loadingTheSameFileTwiceDoesNotAddDuplicates() {
        addAllMessages();
        String file = tempDir.resolve("stored.json").toString();
        store.saveStoredToJson(file);

        MessageStore reloaded = new MessageStore();
        assertEquals(2, reloaded.loadStoredFromJson(file));
        assertEquals(0, reloaded.loadStoredFromJson(file));
        assertEquals(2, reloaded.getStoredMessages().size());
    }

    @Test
    void aMissingFileLoadsNothing() {
        assertEquals(0, store.loadStoredFromJson(tempDir.resolve("nothing.json").toString()));
        assertTrue(store.getStoredMessages().isEmpty());
    }

    @Test
    void anInvalidFileLoadsNothing() throws IOException {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, "[ { \"messageId\": \"1\" ");
        assertEquals(0, store.loadStoredFromJson(file.toString()));
        assertTrue(store.getStoredMessages().isEmpty());
    }

    @Test
    void aFileWithoutASenderUsesTheCurrentSender() throws IOException {
        Path file = tempDir.resolve("old.json");
        Files.writeString(file, "[{\"messageId\": \"1100000001\", \"messageHash\": \"11:0:DIDCAKE\","
                + " \"recipient\": \"+27834557896\", \"message\": \"Did you get the cake?\"}]");
        assertEquals(1, store.loadStoredFromJson(file.toString()));
        assertEquals("Sender: Kyle Smith | Recipient: +27834557896", store.displaySenderAndRecipient());
    }

    @Test
    void savingToAFolderThatDoesNotExistReturnsFalse() {
        addAllMessages();
        assertFalse(store.saveStoredToJson(tempDir.resolve("missing").resolve("x.json").toString()));
    }

    // ---------- JsonHelper ----------

    @Test
    void parsingAnEmptyArrayGivesNoObjects() {
        assertTrue(JsonHelper.parseArray("[]").isEmpty());
        assertTrue(JsonHelper.parseArray("  [ ]  ").isEmpty());
    }

    @Test
    void parsingReadsKeysValuesAndEscapes() {
        List<Map<String, String>> objects = JsonHelper.parseArray(
                "[{\"a\": \"one\", \"b\": \"tab\\there \\u0041\"}, {\"c\": 5}]");
        assertEquals(2, objects.size());
        assertEquals("one", objects.get(0).get("a"));
        assertEquals("tab\there A", objects.get(0).get("b"));
        assertEquals("5", objects.get(1).get("c"));
    }

    @Test
    void parsingRejectsTextThatIsNotAnArray() {
        boolean rejected = false;
        try {
            JsonHelper.parseArray("{\"a\": \"b\"}");
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        assertTrue(rejected);
    }
}
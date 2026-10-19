import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Part 2 unit tests, using the test data from the brief.
public class QuickChatTest {

    @TempDir
    Path tempDir;

    // Test Data for Message 1 (Num Messages = 2): select Send.
    Message message1;
    // Test Data for Message 2: select Discard.
    Message message2;

    @BeforeEach
    void setUp() {
        Message.resetMessages();
        // Write stored messages to a temporary file so the tests leave nothing behind.
        Message.storeFile = tempDir.resolve("storedMessages.json").toString();

        message1 = new Message("0012345678", 0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        message2 = new Message("0112345678", 1, "08575975889", "Hi Keegan, did you receive the payment?");
    }

    @AfterEach
    void tearDown() {
        Message.resetMessages();
        Message.storeFile = "storedMessages.json";
    }

    // ---------- Message length (assertEquals) ----------

    @Test
    void messageNotMoreThan250CharactersSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength(message1.getMessageText()));
    }

    @Test
    void messageOfExactly250CharactersSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    void messageMoreThan250CharactersFailure() {
        assertEquals("Message exceeds 250 characters by 1; please reduce the size.",
                Message.checkMessageLength("a".repeat(251)));
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength("a".repeat(260)));
    }

    @Test
    void emptyMessageIsRejected() {
        assertEquals("Message cannot be empty.", Message.checkMessageLength("   "));
    }

    // ---------- Recipient number (assertEquals) ----------

    @Test
    void recipientNumberCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    void recipientNumberIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                message2.checkRecipientCell());
    }

    @Test
    void recipientWithTooManyDigitsIsRejected() {
        assertEquals(Message.CELL_ERROR, Message.checkRecipientCell("+27712345678901"));
    }

    @Test
    void recipientWithLettersIsRejected() {
        assertEquals(Message.CELL_ERROR, Message.checkRecipientCell("+27abc4567"));
    }

    // ---------- Message hash (assertEquals) ----------

    @Test
    void messageHashForTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
        assertEquals("00:0:HITONIGHT", message1.getMessageHash());
    }

    @Test
    void remainingMessageHashesAreCorrectInALoop() {
        String[] ids = {"0112345678", "1234567890", "5500000000", "9876543210"};
        int[] numbers = {1, 2, 3, 4};
        String[] texts = {
            "Hi Keegan, did you receive the payment?",
            "Did you get the cake?",
            "Yohoooo, I am at your gate.",
            "It is dinner time !"
        };
        String[] expected = {
            "01:1:HIPAYMENT",
            "12:2:DIDCAKE",
            "55:3:YOHOOOOGATE",
            "98:4:ITTIME"
        };

        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], numbers[i], "+27718693002", texts[i]);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    @Test
    void hashIsAllCapitals() {
        Message m = new Message("0012345678", 1, "+27718693002", "good morning friend");
        assertEquals(m.getMessageHash(), m.getMessageHash().toUpperCase());
    }

    @Test
    void hashForSingleWordMessageUsesWordTwice() {
        Message m = new Message("0012345678", 0, "+27718693002", "Hello");
        assertEquals("00:0:HELLOHELLO", m.getMessageHash());
    }

    // ---------- Message ID ----------

    @Test
    void messageIdIsCreated() {
        assertEquals("Message ID generated: 0012345678", message1.messageIdGenerated());
        assertTrue(message1.checkMessageID());
    }

    @Test
    void automaticallyGeneratedIdIsTenDigits() {
        Message m = new Message(0, "+27718693002", "Hi there");
        assertEquals(10, m.getMessageId().length());
        assertTrue(m.getMessageId().matches("[0-9]{10}"));
        assertTrue(m.checkMessageID());
    }

    @Test
    void idLongerThanTenCharactersFailsTheCheck() {
        Message m = new Message("12345678901", 0, "+27718693002", "Hi there");
        assertFalse(m.checkMessageID());
    }

    // ---------- MessageSent (assertEquals) ----------

    @Test
    void userSelectedSendMessage() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void userSelectedDisregardMessage() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    void userSelectedStoreMessage() {
        assertEquals("Message successfully stored.", message1.sentMessage(3));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    void invalidChoiceIsReported() {
        assertEquals(Message.MSG_INVALID_OPTION, message1.sentMessage(9));
    }

    // ---------- sentMessage(Scanner), the interactive version ----------

    @Test
    void scannerChoosingSendSendsTheMessage() {
        assertEquals(Message.MSG_SENT, message1.sentMessage(new Scanner("1\n")));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void scannerDisregardAndPressingZeroDeletesTheMessage() {
        assertEquals(Message.MSG_DELETED, message2.sentMessage(new Scanner("2\n0\n")));
        assertEquals(0, Message.returnTotalMessages());
    }

    @Test
    void scannerDisregardWithoutZeroKeepsAskingUntilUserChooses() {
        assertEquals(Message.MSG_SENT, message1.sentMessage(new Scanner("2\nx\n1\n")));
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void scannerInvalidChoiceAsksAgain() {
        assertEquals(Message.MSG_SENT, message1.sentMessage(new Scanner("9\n1\n")));
    }

    @Test
    void scannerChoosingStoreStoresTheMessage() {
        assertEquals(Message.MSG_STORED, message1.sentMessage(new Scanner("3\n")));
        assertEquals(0, Message.returnTotalMessages());
    }

    // ---------- Total number sent (Num Messages = 2) ----------

    @Test
    void totalNumberSentAfterTestData() {
        assertEquals(0, Message.returnTotalMessages());
        message1.sentMessage(1);   // Select Send
        message2.sentMessage(2);   // Select Discard
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void totalGrowsWithEachSentMessage() {
        new Message("0012345678", 0, "+27718693002", "First message").sentMessage(1);
        new Message("0112345678", 1, "+27718693002", "Second message").sentMessage(1);
        new Message("0212345678", 2, "+27718693002", "Third message").sentMessage(1);
        assertEquals(3, Message.returnTotalMessages());
    }

    // ---------- printMessages() ----------

    @Test
    void printMessagesWhenNothingSent() {
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }

    @Test
    void printMessagesListsOnlySentMessagesInTheRequiredOrder() {
        message1.sentMessage(1);
        message2.sentMessage(2);

        String all = Message.printMessages();

        int id = all.indexOf("Message ID: 0012345678");
        int hash = all.indexOf("Message Hash: 00:0:HITONIGHT");
        int recipient = all.indexOf("Recipient: +27718693002");
        int message = all.indexOf("Message: Hi Mike, can you join us for dinner tonight?");
        assertTrue(id >= 0 && id < hash && hash < recipient && recipient < message);
        assertFalse(all.contains("Keegan"));
    }

    @Test
    void storedMessagesAreNotIncludedInPrintMessages() {
        message1.sentMessage(3);
        assertEquals("No messages have been sent yet.", Message.printMessages());
    }

    // ---------- storeMessage() ----------

    @Test
    void storeMessageWritesJsonFile() throws IOException {
        assertTrue(message1.storeMessage());

        String json = Files.readString(Path.of(Message.storeFile));
        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"messageId\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
        assertTrue(json.contains("\"message\": \"Hi Mike, can you join us for dinner tonight?\""));
    }

    @Test
    void storeMessageKeepsEarlierStoredMessages() throws IOException {
        message1.storeMessage();
        message2.storeMessage();

        String json = Files.readString(Path.of(Message.storeFile));
        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("0112345678"));
    }

    @Test
    void storeMessageEscapesQuotesInTheMessage() throws IOException {
        Message m = new Message("0012345678", 0, "+27718693002", "She said \"hi\" today");
        m.storeMessage();

        String json = Files.readString(Path.of(Message.storeFile));
        assertTrue(json.contains("She said \\\"hi\\\" today"));
    }

    @Test
    void storeMessageReturnsFalseWhenFileCannotBeWritten() {
        Message.storeFile = tempDir.resolve("missing-folder").resolve("x.json").toString();
        assertFalse(message1.storeMessage());
        assertEquals(Message.MSG_STORE_FAILED, message1.sentMessage(3));
    }
}
package finance.cli;

import static org.assertj.core.api.Assertions.assertThat;

import finance.model.OperationType;
import finance.model.User;
import finance.persistence.Storage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class FinanceCliTest {
    @AfterEach
    void removeTestStorage() throws Exception {
        Files.deleteIfExists(Storage.FILE);
        Files.deleteIfExists(Storage.FILE.getParent());
    }

    @Test
    void runsFinanceWorkflowAndPersistsBothSidesOfTransfer() {
        String commands = String.join("\n",
                "register alice pass",
                "category add food",
                "expense food 300",
                "income salary 1000",
                "budget food 200",
                "logout",
                "register bob pass",
                "logout",
                "login alice pass",
                "transfer bob 50",
                "report food",
                "exit") + "\n";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream previousOutput = System.out;
        java.io.InputStream previousInput = System.in;
        try {
            System.setIn(new ByteArrayInputStream(commands.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            new FinanceCli().run();
        } finally {
            System.setIn(previousInput);
            System.setOut(previousOutput);
        }

        String text = output.toString(StandardCharsets.UTF_8);
        assertThat(text).contains("Внимание: превышен бюджет категории «food»!", "Оставшийся бюджет: -100.00");
        User alice = Storage.load().get("alice");
        User bob = Storage.load().get("bob");
        assertThat(alice.getWallet().total(OperationType.EXPENSE)).isEqualByComparingTo("350");
        assertThat(bob.getWallet().total(OperationType.INCOME)).isEqualByComparingTo("50");
    }
}

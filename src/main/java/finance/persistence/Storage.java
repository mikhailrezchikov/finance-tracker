package finance.persistence;

import finance.model.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Storage {
    public static final Path FILE = Path.of("data", "finance-tracker.bin");

    private Storage() {
    }

    public static Map<String, User> load() {
        if (!Files.exists(FILE)) {
            return new LinkedHashMap<>();
        }
        try (ObjectInputStream stream = new ObjectInputStream(Files.newInputStream(FILE))) {
            return (Map<String, User>) stream.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException exception) {
            System.err.println("Не удалось загрузить сохранённые данные: " + exception.getMessage());
            return new LinkedHashMap<>();
        }
    }

    public static void save(Map<String, User> users) {
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = Files.createTempFile(FILE.getParent(), "finance-", ".tmp");
            try (ObjectOutputStream stream = new ObjectOutputStream(Files.newOutputStream(temporary))) {
                stream.writeObject(users);
            }
            Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            System.err.println("Не удалось сохранить данные: " + exception.getMessage());
        }
    }
}

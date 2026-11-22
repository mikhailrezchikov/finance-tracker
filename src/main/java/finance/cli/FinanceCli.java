package finance.cli;

import finance.model.Category;
import finance.model.Money;
import finance.model.OperationType;
import finance.model.User;
import finance.persistence.Storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class FinanceCli {
    private final Scanner input = new Scanner(System.in);
    private final Map<String, User> users = Storage.load();
    private User currentUser;

    private static void requireCount(List<String> args, int expected, String usage) {
        if (args.size() != expected) {
            throw new IllegalArgumentException("использование: " + usage);
        }
    }

    private static void println(String text) {
        System.out.println(text);
    }

    private static List<String> tokenize(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        for (char character : line.trim().toCharArray()) {
            if (character == '\"') {
                quoted = !quoted;
            } else if (Character.isWhitespace(character) && !quoted) {
                if (!token.isEmpty()) {
                    result.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(character);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Незакрытая кавычка");
        }
        if (!token.isEmpty()) {
            result.add(token.toString());
        }
        return result;
    }

    public void run() {
        println("Finance Tracker. Введите 'help' для списка команд.");
        while (true) {
            System.out.print(currentUser == null ? "guest> " : currentUser.getLogin() + "> ");
            if (!input.hasNextLine()) {
                save();
                return;
            }
            try {
                if (handle(tokenize(input.nextLine()))) {
                    save();
                    println("Данные сохранены. До свидания!");
                    return;
                }
            } catch (IllegalArgumentException exception) {
                println("Ошибка: " + exception.getMessage());
            } catch (IOException exception) {
                println("Ошибка файла: " + exception.getMessage());
            }
        }
    }

    private void category(List<String> args) {
        if (args.size() == 2 && args.get(1).equalsIgnoreCase("list")) {
            println("Категории расходов: " + String.join(", ", currentUser.getWallet().getCategories().keySet()));
            return;
        }
        requireCount(args, 3, "category add <название> | category list");
        if (!args.get(1).equalsIgnoreCase("add")) {
            throw new IllegalArgumentException("используйте category add <название>");
        }
        println(currentUser.getWallet().addCategory(args.get(2)) ? "Категория добавлена: " + args.get(2) : "Категория уже существует.");
        save();
    }

    private void operation(List<String> args, OperationType type) {
        requireCount(args, 3, (type == OperationType.INCOME ? "income" : "expense") + " <категория> <сумма>");
        String category = args.get(1);
        if (type == OperationType.EXPENSE && !currentUser.getWallet().getCategories().containsKey(category)) {
            throw new IllegalArgumentException("категория расходов не найдена: " + category);
        }
        BigDecimal value = Money.parsePositive(args.get(2));
        currentUser.getWallet().addOperation(type, category, value);
        save();
        println((type == OperationType.INCOME ? "Доход" : "Расход") + " добавлен: " + category + " — " + Money.format(value));
        alerts(category);
    }

    private void budget(List<String> args) {
        requireCount(args, 3, "budget <категория> <сумма>");
        Category category = currentUser.getWallet().getCategories().get(args.get(1));
        if (category == null) {
            throw new IllegalArgumentException("категория не найдена: " + args.get(1));
        }
        category.setBudget(Money.parsePositive(args.get(2)));
        save();
        println("Бюджет установлен: " + category.getName() + " — " + Money.format(category.getBudget()));
        alerts(category.getName());
    }

    private void register(List<String> args) {
        requireCount(args, 3, "register <логин> <пароль>");
        String login = args.get(1);
        if (!login.matches("[A-Za-zА-Яа-я0-9_.-]{3,30}")) {
            throw new IllegalArgumentException("логин: 3–30 букв, цифр, '.', '_' или '-'");
        }
        if (args.get(2).length() < 4) {
            throw new IllegalArgumentException("пароль должен содержать не менее 4 символов");
        }
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("пользователь уже существует");
        }
        currentUser = new User(login, args.get(2));
        users.put(login, currentUser);
        save();
        println("Пользователь создан и авторизован.");
    }

    private void login(List<String> args) {
        requireCount(args, 3, "login <логин> <пароль>");
        User user = users.get(args.get(1));
        if (user == null || !user.hasPassword(args.get(2))) {
            throw new IllegalArgumentException("неверный логин или пароль");
        }
        currentUser = user;
        println("Выполнен вход: " + user.getLogin() + ". Кошелёк загружен.");
    }

    private void transfer(List<String> args) {
        requireCount(args, 3, "transfer <логин-получателя> <сумма>");
        User receiver = users.get(args.get(1));
        if (receiver == null) {
            throw new IllegalArgumentException("получатель не найден: " + args.get(1));
        }
        if (receiver == currentUser) {
            throw new IllegalArgumentException("нельзя переводить самому себе");
        }
        BigDecimal value = Money.parsePositive(args.get(2));
        currentUser.getWallet().addOperation(OperationType.EXPENSE, "Перевод: " + receiver.getLogin(), value);
        receiver.getWallet().addOperation(OperationType.INCOME, "Перевод от " + currentUser.getLogin(), value);
        save();
        println("Перевод выполнен: " + Money.format(value));
        alerts(null);
    }

    private boolean handle(List<String> args) throws IOException {
        if (args.isEmpty()) {
            return false;
        }
        String command = args.get(0).toLowerCase(Locale.ROOT);
        if (command.equals("exit") || command.equals("quit")) {
            return true;
        }
        if (command.equals("help")) {
            help();
            return false;
        }
        if (command.equals("register")) {
            register(args);
            return false;
        }
        if (command.equals("login")) {
            login(args);
            return false;
        }
        if (command.equals("logout")) {
            requireUser();
            currentUser = null;
            println("Вы вышли из учётной записи.");
            return false;
        }
        requireUser();
        switch (command) {
            case "category" -> category(args);
            case "income" -> operation(args, OperationType.INCOME);
            case "expense" -> operation(args, OperationType.EXPENSE);
            case "budget" -> budget(args);
            case "report" -> report(args);
            case "transfer" -> transfer(args);
            case "save" -> {
                save();
                println("Данные сохранены.");
            }
            default -> println("Неизвестная команда. Введите 'help'.");
        }
        return false;
    }

    private void report(List<String> args) throws IOException {
        List<String> selected = new ArrayList<>();
        String filename = null;
        for (int index = 1; index < args.size(); index++) {
            if (args.get(index).equalsIgnoreCase("file")) {
                if (++index == args.size()) {
                    throw new IllegalArgumentException("после file укажите путь");
                }
                filename = args.get(index);
            } else {
                selected.add(args.get(index));
            }
        }
        String content = "Пользователь: " + currentUser.getLogin() + System.lineSeparator() + currentUser.getWallet().createReport(selected);
        if (filename == null) {
            System.out.print(content);
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename))) {
                writer.write(content);
            }
            println("Отчёт записан в " + Path.of(filename).toAbsolutePath());
        }
    }

    private void alerts(String categoryName) {
        if (categoryName != null) {
            Category category = currentUser.getWallet().getCategories().get(categoryName);
            if (category != null && category.getBudget() != null && currentUser.getWallet().amountByCategory(OperationType.EXPENSE, categoryName).compareTo(category.getBudget()) > 0) {
                println("Внимание: превышен бюджет категории «" + categoryName + "»!");
            }
        }
        if (currentUser.getWallet().total(OperationType.EXPENSE).compareTo(currentUser.getWallet().total(OperationType.INCOME)) > 0) {
            println("Внимание: общие расходы превысили доходы!");
        }
    }

    private void help() {
        println("""
                Команды:
                register <логин> <пароль>
                login <логин> <пароль>
                logout,
                category add <название>
                category list
                income <категория> <сумма>
                expense <категория> <сумма>
                budget <категория> <сумма>
                report [категория ...] [file <путь>]
                transfer <логин> <сумма>, save, exit.
                """);
    }

    private void requireUser() {
        if (currentUser == null) {
            throw new IllegalArgumentException("сначала выполните вход (login) или регистрацию (register)");
        }
    }

    private void save() {
        Storage.save(users);
    }
}

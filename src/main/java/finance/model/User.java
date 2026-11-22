package finance.model;

import java.io.Serializable;

public final class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String login;
    private final String password;
    private final Wallet wallet = new Wallet();

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public boolean hasPassword(String candidate) {
        return password.equals(candidate);
    }

    public Wallet getWallet() {
        return wallet;
    }
}

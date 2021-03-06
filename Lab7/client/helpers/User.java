package helpers;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author Sabitov Danil
 * @version 1.0
 * Class for describing User
 */
public class User {
    private String login;
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof User) {
            User userObj = (User) o;
            return login.equals(userObj.getLogin()) && password.equals(userObj.getPassword());
        }
        return false;
    }



    @Override
    public String toString() {
        return login + ","
                + password;
    }
}
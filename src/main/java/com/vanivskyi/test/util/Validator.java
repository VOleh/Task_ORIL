package com.vanivskyi.test.util;

import com.vanivskyi.test.exception.InvalidEmailException;
import com.vanivskyi.test.exception.InvalidPasswordException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    private static final String PASSWORD_PATTERN = "^[a-zA-Z0-9]{4,15}$";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    public static boolean validateEmail(String email) throws InvalidEmailException {
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    public static boolean validatePassword(String password) throws InvalidPasswordException {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}
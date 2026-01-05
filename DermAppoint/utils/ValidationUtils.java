package utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^09\\d{9}$");
    private static final Pattern NAME_PATTERN = 
        Pattern.compile("^[A-Za-z\\s.'-]+$");
    
    public static boolean validateEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean validatePhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean validateName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches() && name.length() >= 2;
    }
    
    public static boolean validateAge(int age) {
        return age > 0 && age < 150;
    }
    
    public static boolean validateDateNotPast(LocalDate date) {
        return !date.isBefore(LocalDate.now());
    }
    
    public static boolean validatePassword(String password) {
        return password != null && password.length() >= 8;
    }
}
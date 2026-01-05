package UI;

public interface ILogin {
    boolean login(String email, String password);
    void logout();
    boolean changePassword(String oldPassword, String newPassword);
}
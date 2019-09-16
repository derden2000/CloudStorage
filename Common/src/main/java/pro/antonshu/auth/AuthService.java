package pro.antonshu.auth;

public interface AuthService {

    boolean authUser(String login, String password);

    boolean regNewUser(String login, String password);
}

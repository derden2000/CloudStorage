package pro.antonshu.service;

public interface UserRepository {

    boolean authUser(String login, String password);

    boolean regNewUser(String login, String password);
}

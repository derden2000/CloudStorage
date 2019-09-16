package pro.antonshu.service;

import java.util.HashMap;
import java.util.Map;

public class UserRepositoryImpl implements UserRepository {

    private static Map<String, String> users = new HashMap<>();

    public UserRepositoryImpl() {
        users.put("ivan", "123");
        users.put("petr", "345");
        users.put("123", "123");
    }


    @Override
    public boolean authUser(String login, String password) {
        String pwd = users.get(login);
        return pwd != null && pwd.equals(password);
    }

    @Override
    public boolean regNewUser(String login, String password) {
        users.put(login, password);
        return true;
    }
}

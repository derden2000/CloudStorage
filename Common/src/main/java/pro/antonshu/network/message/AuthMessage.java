package pro.antonshu.network.message;

public class AuthMessage extends Message {

    private String login;
    private String password;
    private boolean authorize;
    private boolean regMarker;

    public boolean getAuthorize() {
        return authorize;
    }

    public void setAuthorize(boolean authorize) {
        this.authorize = authorize;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean getRegMarker() {
        return regMarker;
    }

    public AuthMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public AuthMessage(String login, String password, boolean regMarker) {
        this.login = login;
        this.password = password;
        this.regMarker = regMarker;
    }
}

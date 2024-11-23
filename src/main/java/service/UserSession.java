package service;

import java.util.prefs.Preferences;

public class UserSession {

    private static volatile UserSession instance;
    private String userName;
    private String password;
    private String privileges;
    private Preferences userPreferences;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        this.userPreferences = Preferences.userRoot().node(this.getClass().getName());
        saveToPreferences();
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        if (instance == null) {
            synchronized (UserSession.class) {
                if (instance == null) {
                    instance = new UserSession(userName, password, privileges);
                }
            }
        }
        return instance;
    }

    private void saveToPreferences() {
        userPreferences.put("USERNAME",userName);
        userPreferences.put("PASSWORD",password);
        userPreferences.put("PRIVILEGES",privileges);
    }

    public void loadFromPreferences(){
        userName = userPreferences.get("USERNAME","");
        password = userPreferences.get("PASSWORD","");
        privileges = userPreferences.get("PRIVILEGES","");

    }

    public void cleanUserSession() {
        userName = "";
        password = "";
        privileges = "";
        userPreferences.remove("USERNAME");
        userPreferences.remove("PASSWORD");
        userPreferences.remove("PRIVILEGES");
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userName='" + this.userName + '\'' +
                ", privileges=" + this.privileges +
                '}';
    }
}

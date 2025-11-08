package util;

import com.google.gson.JsonObject;

import model.User;

/**
 * Utility class để parse User từ JSON, tránh duplicate code
 */
public class UserParser {
    
    /**
     * Parse User từ JsonObject
     * @param userObject JsonObject chứa thông tin user
     * @return User object
     */
    public static User parseFromJson(JsonObject userObject) {
        User user = new User();
        
        if (userObject.has("id")) {
            user.setId(userObject.get("id").getAsInt());
        }
        if (userObject.has("username")) {
            user.setUsername(userObject.get("username").getAsString());
        }
        if (userObject.has("name")) {
            user.setName(userObject.get("name").getAsString());
        }
        if (userObject.has("avatar")) {
            user.setAvatar(userObject.get("avatar").getAsString());
        }
        if (userObject.has("gender")) {
            user.setGender(userObject.get("gender").getAsString());
        }
        if (userObject.has("yearOfBirth")) {
            user.setYearOfBirth(userObject.get("yearOfBirth").getAsInt());
        }
        if (userObject.has("score")) {
            user.setScore(userObject.get("score").getAsDouble());
        }
        if (userObject.has("matchCount")) {
            user.setMatchCount(userObject.get("matchCount").getAsInt());
        }
        if (userObject.has("winCount")) {
            user.setWinCount(userObject.get("winCount").getAsInt());
        }
        if (userObject.has("loseCount")) {
            user.setLoseCount(userObject.get("loseCount").getAsInt());
        }
        if (userObject.has("blocked")) {
            user.setBlocked(userObject.get("blocked").getAsBoolean());
        }
        if (userObject.has("status")) {
            user.setStatus(userObject.get("status").getAsString());
        }
        
        return user;
    }
}

package model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Message {
    private final String type;
    private final JsonObject payload;
    
    public Message(String type) {
        this.type = type;
        this.payload = new JsonObject();
    }
    
    public Message(String type, JsonObject payload) {
        this.type = type;
        this.payload = payload;
    }
    
    public String getType() {
        return type;
    }
    
    public JsonObject getPayload() {
        return payload;
    }
    
    public void addProperty(String key, String value) {
        payload.addProperty(key, value);
    }
    
    public void addProperty(String key, Number value) {
        payload.addProperty(key, value);
    }
    
    public void addProperty(String key, Boolean value) {
        payload.addProperty(key, value);
    }
    
    public String toJson() {
        JsonObject message = new JsonObject();
        message.addProperty("type", type);
        message.add("payload", payload);
        
        return message.toString();
    }
    
    public static Message fromJson(String json) {
        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonObject payload = jsonObject.getAsJsonObject("payload");
        
        return new Message(type, payload);
    }
}
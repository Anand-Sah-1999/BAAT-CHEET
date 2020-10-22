package com.example.baatcheet;

public class PrivateChat_GetterSetter {

    private String message, messageType, senderID, currentTime, currentDate, uniqueMessageKey;

    public PrivateChat_GetterSetter() {

    }

    public PrivateChat_GetterSetter(String message, String messageType, String senderID, String currentTime, String currentDate) {
        this.message = message;
        this.messageType = messageType;
        this.senderID = senderID;
        this.currentTime = currentTime;
        this.currentDate = currentDate;
        this.uniqueMessageKey = uniqueMessageKey;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSenderID() {
        return senderID;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public String getUniqueMessageKey() {
        return uniqueMessageKey;
    }
}

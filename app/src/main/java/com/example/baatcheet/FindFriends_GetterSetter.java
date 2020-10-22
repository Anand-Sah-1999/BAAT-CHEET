package com.example.baatcheet;

public class FindFriends_GetterSetter {

    String imageURL;
    String displayName;
    String status;
    String uniqueUserID;

    public FindFriends_GetterSetter() {
        // Empty constructor required while using Firebase
    }

    public FindFriends_GetterSetter(String imageURL, String displayName, String status, String uniqueUserID) {
        this.imageURL = imageURL;
        this.displayName = displayName;
        this.status = status;
        this.uniqueUserID = uniqueUserID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return status;
    }

    public String getUniqueUserID() {
        return uniqueUserID;
    }
}

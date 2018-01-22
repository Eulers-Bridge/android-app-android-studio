package com.eulersbridge.isegoria.util;

public final class Constants {

    private Constants() {
        // Hide implicit public constructor
    }

    public static final String APP_ID = "au.com.isegoria.app";

    // Server keys/paths
    public static final String SNS_PLATFORM_APPLICATION_ARN = "arn:aws:sns:ap-southeast-2:715927704730:app/GCM/android_dev";

    // Used for storing the user's email to automatically log them in on subsequent app launches
    public static final String USER_EMAIL_KEY = "userEmail";
    public static final String USER_PASSWORD_KEY = "userPassword";

    // Used for home screen long-press app shortcuts on Android 7.1+
    public static final String SHORTCUT_ACTION_ELECTION = "SHORTCUT_ELECTION";
    public static final String SHORTCUT_ACTION_FRIENDS = "SHORTCUT_FRIENDS";

    public static final String ACTIVITY_EXTRA_NEWS_ARTICLE = "article";
    public static final String FRAGMENT_EXTRA_PHOTO_ALBUM = "album";
    public static final String ACTIVITY_EXTRA_PHOTOS = "photos";
    public static final String ACTIVITY_EXTRA_PHOTOS_POSITION = "position";
    public static final String ACTIVITY_EXTRA_EVENT = "event";
    public static final String FRAGMENT_EXTRA_CANDIDATE_POSITION = "position";
    public static final String ACTIVITY_EXTRA_POLL = "poll";
    public static final String FRAGMENT_EXTRA_USER = "user";
    public static final String FRAGMENT_EXTRA_CONTACT = "contact";
    public static final String FRAGMENT_EXTRA_PROFILE_ID = "profileId";

    public static final String NOTIFICATION_CHANNEL_FRIENDS = "Friends";
    public static final String NOTIFICATION_CHANNEL_VOTE_REMINDERS = "Vote Reminder";
}

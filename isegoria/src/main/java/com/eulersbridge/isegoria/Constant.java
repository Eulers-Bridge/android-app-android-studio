package com.eulersbridge.isegoria;

public class Constant {

    // App identifier for logging
    public static final String TAG = "au.com.isegoria.app";

    // Server keys/paths
    public static final String SNS_PLATFORM_APPLICATION_ARN = "arn:aws:sns:ap-southeast-2:715927704730:app/GCM/android_dev";

    public static final String S3_PICTURES_BUCKET_NAME = "isegoriauserpics";
    public static final String S3_PICTURES_PATH = "https://s3.amazonaws.com/isegoriauserpics/";

    public static final String SERVER_URL = "http://54.79.70.241:8080/dbInterface/api/";
    //public static final String PICTURES_URL "https://s3-ap-southeast-2.amazonaws.com/isegoria/";

    // Used for home screen long-press app shortcuts on Android 7.1+
    static final String SHORTCUT_ACTION_ELECTION = "SHORTCUT_ELECTION";
    static final String SHORTCUT_ACTION_FRIENDS = "SHORTCUT_FRIENDS";

    public static final String ACTIVITY_EXTRA_NEWS_ARTICLE = "article";
    public static final String FRAGMENT_EXTRA_PHOTO_ALBUM_ID = "albumId";
    public static final String ACTIVITY_EXTRA_PHOTOS = "photos";
    public static final String ACTIVITY_EXTRA_PHOTOS_POSITION = "position";
    public static final String ACTIVITY_EXTRA_EVENT = "event";
    public static final String FRAGMENT_EXTRA_CANDIDATE_POSITION = "position";
    public static final String ACTIVITY_EXTRA_POLL = "poll";
    public static final String FRAGMENT_EXTRA_USER = "user";
    public static final String FRAGMENT_EXTRA_PROFILE_ID = "profileId";
}

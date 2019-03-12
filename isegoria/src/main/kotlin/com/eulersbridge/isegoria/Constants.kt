@file:JvmName("Constants")

package com.eulersbridge.isegoria

const val APP_ID = "au.com.isegoria.app"

// Server keys/paths
const val SNS_PLATFORM_APPLICATION_ARN = "arn:aws:sns:ap-southeast-2:715927704730:app/GCM/android_dev"

// Used for storing the user's email to automatically log them in on subsequent app launches
const val USER_EMAIL_KEY = "userEmail"
const val USER_PASSWORD_KEY = "userPassword"
const val ENDPOINT_ARN_KEY = "endpointArn"
const val SERVER_URL_KEY = "serverURL"

// Used for home screen long-press app shortcuts on Android 7.1+
const val SHORTCUT_ACTION_ELECTION = "SHORTCUT_ELECTION"
const val SHORTCUT_ACTION_FRIENDS = "SHORTCUT_FRIENDS"

const val FRAGMENT_EXTRA_USER = "user"
const val FRAGMENT_EXTRA_CONTACT = "contact"
const val FRAGMENT_EXTRA_PROFILE_ID = "profileId"
const val FRAGMENT_EXTRA_CANDIDATE_ID = "candidateId"

const val NOTIFICATION_CHANNEL_FRIENDS = "Friends"
const val NOTIFICATION_CHANNEL_VOTE_REMINDERS = "Vote Reminder"
package com.example.proplanetperson.models

// This is your data class for Notification.
// Ensure properties are 'var' or 'val' and NOT 'private var'
data class Notification(
    var userid: String = "", // The ID of the user who triggered the notification
    var text: String = "",   // The notification text (e.g., "started following you")
    var postid: String = "", // The ID of the post, if notification is post-related
    var ispost: Boolean = false // True if notification is for a post, false otherwise
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", "", false)
}
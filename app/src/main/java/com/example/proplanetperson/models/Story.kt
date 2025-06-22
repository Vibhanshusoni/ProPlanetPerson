// Story.kt
package com.example.proplanetperson.models

data class Story(
    var imageUrl: String = "",
    var timeStart: Long = 0,
    var timeEnd: Long = 0,
    var storyId: String = "",
    var userId: String = ""
) {
    // No-argument constructor required for Firebase
    constructor() : this("", 0, 0, "", "")
}
package com.example.proplanetperson.models

data class User(
    var username: String = "",
    var uid: String = "",
    var bio: String = "",
    var fullname: String = "",
    var image: String = ""
) {
    constructor() : this("", "", "", "", "")
}

package com.example.brazedemo

data class CustomCards(
    val title: String?,
    val description: String?,
    val extras: MutableMap<String, String>?,
    val imageUrl: String?,
    val url: String?
) {
    fun writeJson() {
        println("title : $title, description : $description")
    }
}
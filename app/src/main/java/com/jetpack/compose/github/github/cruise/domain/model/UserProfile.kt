package com.jetpack.compose.github.github.cruise.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Created by Dinakar Maurya on 2024/05/14.
 */
@JsonClass(generateAdapter = true)
data class UserProfile(
    val id: Long = 0,
    @Json(name = "avatar_url")
    val avatarUrl: String = "",
    val login: String,
    // nullable as per api doc
    /*
     "name": {
          "type": [
            "string",
            "null"
          ],
     */
    val name: String? = "",
    val followers: Int = 0,
    val following: Int = 0,
    @Json(name = "public_repos")
    val publicRepos: Int = 0,
)

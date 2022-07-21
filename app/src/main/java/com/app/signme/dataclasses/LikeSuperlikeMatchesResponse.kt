package com.app.signme.dataclasses


import com.fasterxml.jackson.annotation.JsonProperty

data class LikeSuperlikeMatchesResponse(
    @JsonProperty("like_user_count")
    val likeUserCount: String?="",
    @JsonProperty("like_user_list")
    val likeUserList: List<LikesResponse>?=null,
    @JsonProperty("match_user_count")
    val matchUserCount: String?="",
    @JsonProperty("match_user_list")
    val matchUserList: List<MatchesResponse>?=null,
    @JsonProperty("superlike_user_count")
    val superlikeUserCount: String?="",
    @JsonProperty("superlike_user_list")
    val superlikeUserList: List<SuperLikesResponse>?=null
)
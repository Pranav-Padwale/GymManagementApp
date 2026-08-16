package com.gym.server.documents

import org.bson.codecs.pojo.annotations.BsonId

data class UserDocument(
    @BsonId
    val username: String, // Treat username as ID since it's unique
    val passwordHash: String,
    val role: String // "SUPER_ADMIN", "TRAINER", "MEMBER"
)

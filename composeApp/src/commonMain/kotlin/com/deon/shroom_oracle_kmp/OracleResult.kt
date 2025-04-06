package com.deon.shroom_oracle_kmp//package com.deon.shroom_oracle_kmp

import kotlinx.serialization.Serializable

@Serializable
data class OracleResult(
    val name: String,
    val confidence: Double,
    val isEdible: Boolean,
    val infoUrl: String? = null
)

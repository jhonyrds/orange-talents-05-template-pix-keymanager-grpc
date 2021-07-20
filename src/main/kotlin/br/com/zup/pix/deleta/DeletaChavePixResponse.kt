package br.com.zup.pix.deleta

import java.time.LocalDateTime

data class DeletaChavePixResponse(
    val key: String,
    val participant: String,
    val deleteAt: LocalDateTime = LocalDateTime.now()
)
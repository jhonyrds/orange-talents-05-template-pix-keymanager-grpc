package br.com.zup.pix.registra

import java.time.LocalDateTime

data class CriaChavePixResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner : Owner,
    val createdAt: LocalDateTime

    )
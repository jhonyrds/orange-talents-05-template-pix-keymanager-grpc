package br.com.zup.pix.registra

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
class ContaAssociada(

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Size(max = 11)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Size(max = 4)
    val agencia: String,

    @field:NotBlank
    @field:Size(max = 6)
    val numeroDaConta: String
) {
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    val idConta: Long? = null
}

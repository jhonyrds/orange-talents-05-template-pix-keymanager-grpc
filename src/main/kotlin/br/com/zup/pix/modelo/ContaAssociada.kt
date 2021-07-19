package br.com.zup.pix.modelo

import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Embeddable
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

}

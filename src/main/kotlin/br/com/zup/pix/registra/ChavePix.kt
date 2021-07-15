package br.com.zup.pix.registra


import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    val clientId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipo: TipoDeChave,

    @field:NotBlank
    val chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoDeConta: TipoDeConta,

    @field: ManyToOne(cascade = [CascadeType.ALL])
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    val criadaEm : LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clientId=$clientId, tipoDeChave=$tipo, chave='$chave', tipoDeConta=$tipoDeConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }


}
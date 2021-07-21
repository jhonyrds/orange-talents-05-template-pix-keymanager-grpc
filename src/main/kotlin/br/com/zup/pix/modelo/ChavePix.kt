package br.com.zup.pix.modelo


import br.com.zup.pix.registra.TipoDeChave
import br.com.zup.pix.registra.TipoDeConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipo: TipoDeChave,

    @field:NotBlank
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoDeConta: TipoDeConta,

    @Embedded
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue
    val id: UUID? = null

    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clientId=$clienteId, tipoDeChave=$tipo, chave='$chave', tipoDeConta=$tipoDeConta, conta=$conta, id=$id, criadaEm=$criadaEm)"
    }

    fun pertenceAo(clienteId: UUID) = this.clienteId.equals(clienteId)

    fun isAleatoria(): Boolean{
        return tipo == TipoDeChave.ALEATORIA
    }

    fun atualiza(chave: String): Boolean {
        if (isAleatoria()){
            this.chave = chave
            return true
        }
        return false
    }


}
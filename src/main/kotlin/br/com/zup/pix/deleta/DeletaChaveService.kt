package br.com.zup.pix.deleta

import br.com.zup.pix.exception.ChavePixNaoEncontradaException
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.servicosExternos.BancoCentralDoBrasilClient
import br.com.zup.pix.util.ValidUUID
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalArgumentException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class DeletaChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BancoCentralDoBrasilClient
) {
    fun deleta(
        @NotBlank @ValidUUID(message = "Cliente ID com o formato inválido") clienteId: String?,
        @NotBlank @ValidUUID(message = "pix ID com formato inválido") pixId: String?
    ) {

        val uuidPixId = UUID.fromString(pixId)
        val uuidClienteId = UUID.fromString(clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException("Chave pix não encontrada ou não pertence ao cliente") }

        val request = DeletaChavePixRequest(chave.chave)
        val bcbResponse = bcbClient.deleta(chave.chave, request)
        if (bcbResponse.status != HttpStatus.OK) {
            throw IllegalStateException("erro ao remover a chave pix do BCB")
        }

        repository.deleteById(uuidPixId)

    }
}
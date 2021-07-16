package br.com.zup.pix.registra

import br.com.zup.pix.servicosExternos.ContasDeClientsNoItauClient
import io.micronaut.validation.Validated
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientsNoItauClient,
) {
    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)

    fun registra(@Valid novaChave: NovaChavePix): ChavePix {

        if (repository.existsByChave(novaChave.chave!!)) {
            throw IllegalArgumentException("Chave pix: ${novaChave.chave} existente")
        }

        val response = itauClient.buscaContaPorTipo(novaChave.clientId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado")

        val chave = novaChave.toModel(conta)
        repository.save(chave)
        LOGGER.info("Chave salva")

        return chave
    }
}
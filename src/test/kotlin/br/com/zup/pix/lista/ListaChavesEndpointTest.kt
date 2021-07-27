package br.com.zup.pix.lista

import br.com.zup.ListaChavesRequest
import br.com.zup.PixListaChavesServiceGrpc
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave
import br.com.zup.pix.registra.TipoDeConta.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixListaChavesServiceGrpc.PixListaChavesServiceBlockingStub
) {
    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup(){
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "teste@teste.com.br", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.CPF, chave = "12345678910", clienteId = CLIENTE_ID))
        repository.save(chave(tipo = TipoDeChave.CELULAR, chave = "+55123456789", clienteId = CLIENTE_ID))
    }

    @AfterEach
    fun clean(){
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente`(){

        //cenário
        val clienteId = CLIENTE_ID.toString()

        //ação
        val response = grpcClient.lista(ListaChavesRequest.newBuilder()
            .setClienteId(clienteId)
            .build())

        //validação
        with(response.chavesList) {
            assertThat(this, hasSize(3))
        }

    }

    @Test
    fun `nao deve fazer a listagem de chaves quando nao tiver chaves cadastradas`() {

        //cenário
        val semChaves = UUID.randomUUID().toString()

        //ação
        val response = grpcClient.lista(ListaChavesRequest.newBuilder()
            .setClienteId(semChaves)
            .build()
        )

        //validação
        assertEquals(0, response.chavesCount)

    }

    @Test
    fun `nao deve fazer a listagem de chaves quando o cliente id for invalido`() {

        //cenário
        val clientIdInvalido = ""

        //ação
        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.lista(ListaChavesRequest.newBuilder()
                .setClienteId(clientIdInvalido)
                .build())
        }

        //validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }


    @Factory
    class Clients {
        @Bean
        fun blochingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixListaChavesServiceGrpc.PixListaChavesServiceBlockingStub? {
            return PixListaChavesServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun chave(
        tipo: TipoDeChave,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePix {
        return ChavePix(
            clienteId = clienteId,
            tipo = tipo,
            chave = chave,
            tipoDeConta = CONTA_CORRENTE,
            conta = ContaAssociada(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Zupper Ajuda Zupper",
                cpfDoTitular = "12345678910",
                agencia = "1234",
                numeroDaConta = "123456"
            )
        )
    }
}
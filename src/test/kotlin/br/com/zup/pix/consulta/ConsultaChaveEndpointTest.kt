package br.com.zup.pix.consulta

import br.com.zup.ConsultaChaveRequest
import br.com.zup.ConsultaChaveRequest.FiltroPorPixId
import br.com.zup.PixConsultaChaveServiceGrpc
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave
import br.com.zup.pix.registra.TipoDeChave.*
import br.com.zup.pix.registra.TipoDeConta
import br.com.zup.pix.registra.TipoDeConta.*
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.servicosExternos.BancoCentralDoBrasilClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class ConsultaChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: PixConsultaChaveServiceGrpc.PixConsultaChaveServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralDoBrasilClient

    companion object {
        val clientId = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        repository.save(chave(tipo = EMAIL, chave = "rafael.ponte@zup.com.br", clienteId = clientId))
        repository.save(chave(tipo = CPF, chave = "63657520325", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = CELULAR, chave = "+551155554321", clienteId = clientId))
    }

    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve consultar chave informando o clientId e o pixId`() {

        //cenário

        val chaveCadastrada = repository.findByChave("63657520325").get()

        //ação

        val response = grpcClient.consulta(
            ConsultaChaveRequest.newBuilder()
                .setPixId(
                    FiltroPorPixId.newBuilder()
                        .setPixId(chaveCadastrada.id.toString())
                        .setClienteId(chaveCadastrada.clienteId.toString())
                        .build()
                ).build()
        )

        //validação

        with(response) {
            assertEquals(chaveCadastrada.id.toString(), this.pixId)
            assertEquals(chaveCadastrada.clienteId.toString(), this.clienteId)
            assertEquals(chaveCadastrada.tipo.name, this.chave.tipo.name)
            assertEquals(chaveCadastrada.chave, this.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        // ação

        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChaveRequest.newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClienteId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        // validação

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId com o filtro invalido`() {


        //ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(
                ConsultaChaveRequest.newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    ).build()
            )
        }

        //validação
        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid UUID string: ", status.description)
        }


    }


    @MockBean(BancoCentralDoBrasilClient::class)
    fun bcbClient(): BancoCentralDoBrasilClient? {
        return mock(BancoCentralDoBrasilClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixConsultaChaveServiceGrpc.PixConsultaChaveServiceBlockingStub {
            return PixConsultaChaveServiceGrpc.newBlockingStub(channel)
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
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "12345678900",
                agencia = "1218",
                numeroDaConta = "123456"
            )
        )
    }
}

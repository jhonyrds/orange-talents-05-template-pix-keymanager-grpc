package br.com.zup.pix.deleta

import br.com.zup.DeletaChaveRequest
import br.com.zup.PixDeletaServiceGrpc
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave
import br.com.zup.pix.registra.TipoDeConta
import br.com.zup.pix.registra.TipoDeConta.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.servicosExternos.BancoCentralDoBrasilClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeletaChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: PixDeletaServiceGrpc.PixDeletaServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralDoBrasilClient

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoDeChave.EMAIL, chave = "teste@teste.com.br", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.CPF, chave = "63657520325", clienteId = UUID.randomUUID()))
        repository.save(chave(tipo = TipoDeChave.CELULAR, chave = "+551155554321", clienteId = UUID.randomUUID()))
    }

    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deletando chave existente`() {
        //cenário


        `when`(bcbClient.deleta("teste@teste.com.br", DeletaChavePixRequest("teste@teste.com.br")))
            .thenReturn(
                HttpResponse.ok(
                    DeletaChavePixResponse(
                        "teste@teste.com.br",
                        participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                        deleteAt = LocalDateTime.now()
                    )
                )
            )

        val chaveCadastrada = repository.findByChave("teste@teste.com.br").get()

        val response = grpcClient.deleta(
            DeletaChaveRequest.newBuilder()
                .setPixId(chaveCadastrada.id.toString())
                .setClienteId(chaveCadastrada.clienteId.toString())
                .build()
        )


        //validação
        with(response) {
            assertEquals(chaveCadastrada.id.toString(), pixId)
            assertEquals(chaveCadastrada.clienteId.toString(), clienteId)
        }

    }

    @Test
    fun `deve retornar status not_found quando o cliente nao estiver cadastrado`() {

        //cenário

        //ação

        val cadastro = repository.findByChave("63657520325").get()

        val error = assertThrows<StatusRuntimeException> {

            grpcClient.deleta(
                DeletaChaveRequest.newBuilder()
                    .setPixId(cadastro.id.toString())
                    .setClienteId("de95a228-1f27-4ad2-907e-e5a2d816e9bc")
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `deve retornar status not_found quando a chave nao for encontrada`() {
        //cenário

        //ação

        val cadastro = repository.findByChave("+551155554321").get()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChaveRequest.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setClienteId(cadastro.clienteId.toString())
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    internal fun `deve retornar status not_found quando usuario tentar excluir chave de terceiro`() {

        //cenário

        //ação

        val cadastro = repository.findByChave("teste@teste.com.br").get()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChaveRequest.newBuilder()
                    .setPixId(cadastro.id.toString())
                    .setClienteId(UUID.randomUUID().toString())
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @MockBean(BancoCentralDoBrasilClient::class)
    fun bcbClient(): BancoCentralDoBrasilClient? {
        return mock(BancoCentralDoBrasilClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixDeletaServiceGrpc.PixDeletaServiceBlockingStub? {
            return PixDeletaServiceGrpc.newBlockingStub(channel)
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
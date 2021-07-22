package br.com.zup.pix.registra


import br.com.zup.PixKeymanagerGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoDeChave
import br.com.zup.TipoDeConta
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave.EMAIL
import br.com.zup.pix.registra.TipoDeConta.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.servicosExternos.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralDoBrasilClient

    @Inject
    lateinit var itauClient: ContasDeClientsNoItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
        repository.save(chave(tipo = EMAIL, chave = "teste@teste.com.br", clienteId = UUID.randomUUID()))
    }

    @AfterEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix`() {

        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastra(criaChavePixRequest()))
            .thenReturn(HttpResponse.created(criaChavePixResponse()))

        // ação
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("rponte@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        // validação
        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar nova chave pix quando o clientId não estiver cadastrado`() {

        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }
    }

    @Test
    fun `nao pode registrar chave quando nao for possivel registrar no bcb`() {
        // cenário
        `when`(itauClient.buscaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.cadastra(criaChavePixRequest()))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("rponte@gmail.com")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar a chave Pix no BCB", status.description)
        }
    }

    @Test
    fun `não deve registar chave pix com o tipo informado divergente`() {
        //cenário


        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157811")
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("teste@teste.com.br")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve registrar nova chave pix quando for existente`() {

        //cenário
        val existente = repository.findByChave("teste@teste.com.br").get()

        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(existente.clienteId.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("teste@teste.com.br")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
        //validação

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave pix: ${existente.chave} existente", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando dados de entrada forem invalidos`() {

        //cenário

        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId("")
                    .setTipoDeChave(TipoDeChave.DESCONHECIDO)
                    .setChave("")
                    .setTipoDeConta(TipoDeConta.DESCONHECIDO_TIPO)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(BancoCentralDoBrasilClient::class)
    fun bcbClient(): BancoCentralDoBrasilClient? {
        return mock(BancoCentralDoBrasilClient::class.java)
    }

    @MockBean(ContasDeClientsNoItauClient::class)
    fun itauClient(): ContasDeClientsNoItauClient? {
        return mock(ContasDeClientsNoItauClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub? {
            return PixKeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", ContaAssociada.ITAU_UNIBANCO_ISPB),
            agencia = "1218",
            numero = "291900",
            titular = TitularResponse("Rafael Ponte", "63657520325")
        )
    }

    private fun criaChavePixRequest(): CriaChaveChavePixRequest {
        return CriaChaveChavePixRequest(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun criaChavePixResponse(): CriaChavePixResponse {
        return CriaChavePixResponse(
            keyType = PixKeyType.EMAIL,
            key = "rponte@gmail.com",
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = "1218",
            accountNumber = "291900",
            accountType = AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Rafael Ponte",
            taxIdNumber = "63657520325"
        )
    }

    private fun chave(
        tipo: br.com.zup.pix.registra.TipoDeChave,
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
package br.com.zup.pix.registra


import br.com.zup.PixKeymanagerGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoDeChave
import br.com.zup.TipoDeConta
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave.CPF
import br.com.zup.pix.registra.TipoDeConta.CONTA_CORRENTE
import br.com.zup.pix.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub
) {
    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar uma nova chave pix`() {

        //cenário


        //ação
        val response = grpcClient.registra(
            RegistraChavePixRequest.newBuilder()
                .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("email@teste.com.br")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        //valição
        with(response) {
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar nova chave pix quando o clientId não estiver cadastrado`() {

        //cenário


        //ação
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157811")
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("02467781054")
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

        //ação
        val contaAssociada = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val existente = ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = CPF,
            chave = "02467781054",
            tipoDeConta = CONTA_CORRENTE,
            conta = contaAssociada
        )
        repository.save(existente)

        //validação

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("02467781054")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }
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

    @Factory
    class Clients {
        @Singleton
        fun blockingstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceBlockingStub? {
            return PixKeymanagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}
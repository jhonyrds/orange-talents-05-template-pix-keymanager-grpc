package br.com.zup.pix.deleta

import br.com.zup.DeletaChaveRequest
import br.com.zup.PixDeletaServiceGrpc
import br.com.zup.pix.modelo.ChavePix
import br.com.zup.pix.modelo.ContaAssociada
import br.com.zup.pix.registra.TipoDeChave
import br.com.zup.pix.registra.TipoDeConta
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
internal class DeletaChaveEndpointTest(
    private val repository: ChavePixRepository,
    private val grpcClient: PixDeletaServiceGrpc.PixDeletaServiceBlockingStub
) {
    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deletando chave existente`() {
        //cenário


        //ação
        val contaAssociada = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val cadastro = ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.CPF,
            chave = "02467781054",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada
        )
        repository.save(cadastro)

        val response = grpcClient.deleta(DeletaChaveRequest.newBuilder()
            .setPixId(cadastro.id.toString())
            .setClienteId(cadastro.clienteId.toString())
            .build())


        //validação
        with(response) {
            assertNotNull(pixId)
            assertEquals(0, repository.count())
        }

    }

    @Test
    fun `deve retornar status not_found quando o cliente nao estiver cadastrado`() {

        //cenário

        //ação

        val contaAssociada = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val cadastro = ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.ALEATORIA,
            chave = UUID.randomUUID().toString(),
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada
        )
        repository.save(cadastro)

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
            assertEquals("Cliente não cadastrado", status.description)
        }
    }

    @Test
    fun `deve retornar status not_found quando a chave nao for encontrada`() {
        //cenário

        //ação
        val contaAssociada = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val cadastro = ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.CPF,
            chave = "02467781054",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada
        )
        repository.save(cadastro)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChaveRequest.newBuilder()
                    .setPixId("dcb51af9-5677-4a3a-a1aa-f87b48e30582")
                    .setClienteId(cadastro.clienteId.toString())
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não encontrada", status.description)
        }
    }

    @Test
    internal fun `deve retornar status not_found quando usuario tentar excluir chave de terceiro`() {

        //cenário

        //ação

        val contaAssociada1 = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Rafael M C Ponte",
            cpfDoTitular = "02467781054",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val contaAssociada2 = ContaAssociada(
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeDoTitular = "Yuri Matheus",
            cpfDoTitular = "86135457004",
            agencia = "0001",
            numeroDaConta = "123455"
        )

        val usuario1 = ChavePix(
            clienteId = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.CELULAR,
            chave = "11123456789",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada1
        )
        repository.save(usuario1)

        val usuario2 = ChavePix(
            clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            tipo = TipoDeChave.EMAIL,
            chave = "email@teste.com.br",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = contaAssociada2
        )
        repository.save(usuario2)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleta(
                DeletaChaveRequest.newBuilder()
                    .setPixId(usuario1.id.toString())
                    .setClienteId(usuario2.clienteId.toString())
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave informada não pertence a sua conta", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingstub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixDeletaServiceGrpc.PixDeletaServiceBlockingStub? {
            return PixDeletaServiceGrpc.newBlockingStub(channel)
        }
    }

}
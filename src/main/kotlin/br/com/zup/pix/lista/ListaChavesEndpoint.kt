package br.com.zup.pix.lista

import br.com.zup.*
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.util.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesEndpoint(
    @Inject private val repository: ChavePixRepository
) : PixListaChavesServiceGrpc.PixListaChavesServiceImplBase() {

    override fun lista(
        request: ListaChavesRequest,
        responseObserver: StreamObserver<ListaChavesResponse>
    ) {

        if (request.clienteId.isNullOrBlank()) {
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")
        }

        val clientId = UUID.fromString(request.clienteId)
        val chaves = repository.findAllByClienteId(clientId).map { chave ->
            ListaChavesResponse.ChavePix.newBuilder()
                .setPixId(chave.id.toString())
                .setTipo(TipoDeChave.valueOf(chave.tipo.name))
                .setChave(chave.chave)
                .setTipoDeConta(TipoDeConta.valueOf(chave.tipoDeConta.name))
                .setCriadaEm(chave.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }
        responseObserver.onNext(ListaChavesResponse.newBuilder()
            .setClienteId(clientId.toString())
            .addAllChaves(chaves)
            .build())
        responseObserver.onCompleted()
    }
}
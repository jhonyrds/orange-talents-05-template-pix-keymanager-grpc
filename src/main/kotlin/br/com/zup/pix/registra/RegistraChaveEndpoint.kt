package br.com.zup.pix.registra

import br.com.zup.PixKeymanagerGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.RegistraChavePixResponse
import br.com.zup.pix.util.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService,) :
    PixKeymanagerGrpcServiceGrpc.PixKeymanagerGrpcServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {

        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)

        responseObserver?.onNext(
            RegistraChavePixResponse.newBuilder()
                .setClienteId(chaveCriada.clientId.toString())
                .setPixId(chaveCriada.id.toString())
                .build()
        )
        responseObserver?.onCompleted()

    }
}
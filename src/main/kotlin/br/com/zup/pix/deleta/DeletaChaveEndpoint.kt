package br.com.zup.pix.deleta

import br.com.zup.DeletaChaveRequest
import br.com.zup.DeletaChaveResponse
import br.com.zup.PixDeletaServiceGrpc
import br.com.zup.pix.util.ErrorHandler

import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaChaveEndpoint(@Inject val service: DeletaChaveService) :
    PixDeletaServiceGrpc.PixDeletaServiceImplBase() {

    override fun deleta(request: DeletaChaveRequest?, responseObserver: StreamObserver<DeletaChaveResponse>?) {
        service.deleta(request!!.clienteId, request.pixId)

        responseObserver!!.onNext(
            DeletaChaveResponse.newBuilder()
                .setClienteId(request.clienteId)
                .setPixId(request.pixId)
                .build()
        )
        responseObserver.onCompleted()
    }
}

package br.com.zup.pix.deleta

import br.com.zup.DeletaChaveRequest
import br.com.zup.DeletaChaveResponse
import br.com.zup.PixDeletaServiceGrpc
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.util.ErrorHandler
import io.grpc.Status
import io.grpc.stub.StreamObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@ErrorHandler
class DeletaChaveEndpoint(@Inject val repository: ChavePixRepository) :
    PixDeletaServiceGrpc.PixDeletaServiceImplBase() {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)

    override fun deleta(
        request: DeletaChaveRequest?,
        responseObserver: StreamObserver<DeletaChaveResponse>?
    ) {
        val pixId = repository.findById(UUID.fromString(request!!.pixId))
        val clienteId = repository.existsByClientId(UUID.fromString(request.clienteId))

        if (!clienteId) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Cliente não cadastrado")
                    .asRuntimeException()
            )

        } else if (pixId.isEmpty) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Chave não encontrada")
                    .asRuntimeException()
            )

        } else if (pixId.get().clientId != UUID.fromString(request.clienteId)) {
            responseObserver?.onError(
                Status.NOT_FOUND
                    .withDescription("Chave informada não pertence a sua conta")
                    .asRuntimeException()
            )
        }

        try {
            repository.deleteById(UUID.fromString(request.pixId))
            LOGGER.info("Chave deletada")
        } catch (e: ConstraintViolationException) {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Dados inválidos")
                    .asRuntimeException()
            )

        }

        responseObserver?.onNext(DeletaChaveResponse.newBuilder()
            .setPixId(pixId.get().chave)
            .build())
        responseObserver?.onCompleted()

    }
}

package br.com.zup.pix.consulta

import br.com.zup.ConsultaChaveRequest
import br.com.zup.ConsultaChaveResponse
import br.com.zup.PixConsultaChaveServiceGrpc
import br.com.zup.pix.repository.ChavePixRepository
import br.com.zup.pix.servicosExternos.BancoCentralDoBrasilClient
import br.com.zup.pix.util.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChaveEndpoint(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BancoCentralDoBrasilClient,
    @Inject private val validador: Validator
) : PixConsultaChaveServiceGrpc.PixConsultaChaveServiceImplBase() {

    override fun consulta(
        request: ConsultaChaveRequest,
        responseObserver: StreamObserver<ConsultaChaveResponse>
    ) {
        val filtro = request.toModel(validador)
        val chaveInfo = filtro.filtra(repository = repository, bcbClient = bcbClient)

        responseObserver.onNext(CarregaChavePixResponseConverte().converte(chaveInfo))
        responseObserver.onCompleted()
    }
}
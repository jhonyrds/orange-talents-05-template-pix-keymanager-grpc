package br.com.zup.pix.consulta

import br.com.zup.ConsultaChaveResponse
import br.com.zup.TipoDeChave
import br.com.zup.TipoDeConta
import br.com.zup.pix.registra.ChavePixInfo
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverte {
    fun converte(chaveInfo: ChavePixInfo): ConsultaChaveResponse {
        return ConsultaChaveResponse.newBuilder()
            .setClienteId(chaveInfo.clienteId.toString() ?: "")
            .setPixId(chaveInfo.pixId.toString() ?: "")
            .setChave(
                ConsultaChaveResponse.ChavePix
                    .newBuilder()
                    .setTipo(TipoDeChave.valueOf(chaveInfo.tipo.name))
                    .setChave(chaveInfo.chave)
                    .setConta(
                        ConsultaChaveResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipo(TipoDeConta.valueOf(chaveInfo.tipoDeConta.name))
                            .setInstituicao(chaveInfo.conta.instituicao)
                            .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                            .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.registradaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            )
            .build()
    }
}

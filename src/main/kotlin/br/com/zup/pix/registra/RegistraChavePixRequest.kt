package br.com.zup.pix.registra

import br.com.zup.RegistraChavePixRequest
import br.com.zup.TipoDeChave.*
import br.com.zup.TipoDeConta.*


fun RegistraChavePixRequest.toModel() : NovaChavePix {

    return NovaChavePix(
        clienteId,
        tipo = when(tipoDeChave){
            DESCONHECIDO -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            DESCONHECIDO_TIPO -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name) // 1
        }
    )
}
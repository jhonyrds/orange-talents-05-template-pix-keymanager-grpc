package br.com.zup.pix.consulta

import br.com.zup.ConsultaChaveRequest
import br.com.zup.ConsultaChaveRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChaveRequest.toModel(validador: Validator): Filtro {

    val filtro = when(filtroCase!!) {
        PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violacoes = validador.validate(filtro)
    if (violacoes.isNotEmpty()){
        throw ConstraintViolationException(violacoes)
    }

    return filtro
}
package br.com.zup.pix.deleta

import br.com.zup.pix.modelo.ContaAssociada

data class DeletaChavePixRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)
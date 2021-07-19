package br.com.zup.pix.repository

import br.com.zup.pix.modelo.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository : CrudRepository<ChavePix, UUID> {
    fun existsByChave(chave: String?): Boolean
    fun existsByClientId(clientId: UUID?): Boolean

}
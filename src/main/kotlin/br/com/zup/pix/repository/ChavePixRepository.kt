package br.com.zup.pix.repository

import br.com.zup.pix.modelo.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.repository.CrudRepository
import java.util.*

@Repository
interface ChavePixRepository : CrudRepository<ChavePix, UUID> {
    fun findAllByClienteId(clienteId: UUID): List<ChavePix>
    fun existsByChave(chave: String?): Boolean
    fun findByChave(chave: String): Optional<ChavePix>
    fun findByIdAndClienteId(id: UUID, clienteId: UUID): Optional<ChavePix>

}
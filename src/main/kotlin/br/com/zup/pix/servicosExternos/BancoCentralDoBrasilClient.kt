package br.com.zup.pix.servicosExternos

import br.com.zup.pix.deleta.DeletaChavePixRequest
import br.com.zup.pix.deleta.DeletaChavePixResponse
import br.com.zup.pix.registra.ChavePixDetalhesResponse
import br.com.zup.pix.registra.CriaChaveChavePixRequest
import br.com.zup.pix.registra.CriaChavePixResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client


@Client("\${bcb.pix.url}")
interface BancoCentralDoBrasilClient {

    @Post(
        "/api/v1/pix/keys",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun cadastra(@Body request: CriaChaveChavePixRequest): HttpResponse<CriaChavePixResponse>

    @Delete("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun deleta(@PathVariable key: String, @Body request: DeletaChavePixRequest) : HttpResponse<DeletaChavePixResponse>

    @Get("/api/v1/pix/keys/{key}",
        produces = [MediaType.APPLICATION_XML],
        consumes = [MediaType.APPLICATION_XML]
    )
    fun consultaPorChave(@PathVariable key: String): HttpResponse<ChavePixDetalhesResponse>
}
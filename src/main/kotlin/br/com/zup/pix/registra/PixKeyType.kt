package br.com.zup.pix.registra

enum class PixKeyType(val domainType: TipoDeChave?) {
    CPF(TipoDeChave.CPF),
    CNPJ(null),
    PHONE(TipoDeChave.CELULAR),
    EMAIL(TipoDeChave.EMAIL),
    RANDOM(TipoDeChave.ALEATORIA);

    companion object {
        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoDeChave?): PixKeyType {
            return mapping[domainType] ?: throw IllegalArgumentException("Chave pix inválida")
        }
    }
}
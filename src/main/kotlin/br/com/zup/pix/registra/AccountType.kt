package br.com.zup.pix.registra

enum class AccountType {
    CACC,
    SVGS;

    companion object{
        fun by(domainType: TipoDeConta) : AccountType{
            return when (domainType) {
                TipoDeConta.CONTA_CORRENTE -> CACC
                TipoDeConta.CONTA_POUPANCA -> SVGS
            }
        }
    }
}

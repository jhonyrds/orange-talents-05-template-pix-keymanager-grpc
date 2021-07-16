package br.com.zup.pix.registra

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoDeChave {

    EMAIL {
        override fun validaChave(chave: String): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    CELULAR {
        override fun validaChave(chave: String): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }
            return chave.matches("^[0-9]{11}\$".toRegex())
        }
    },
    CPF {
        override fun validaChave(chave: String): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            if (!chave.matches("[0-9]+".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    ALEATORIA {
        override fun validaChave(chave: String): Boolean {
            return true
        }
    };

    abstract fun validaChave(chave: String): Boolean
}
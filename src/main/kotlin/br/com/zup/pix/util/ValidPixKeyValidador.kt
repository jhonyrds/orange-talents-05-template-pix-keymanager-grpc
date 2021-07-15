package br.com.zup.pix.util

import br.com.zup.pix.registra.NovaChavePix
import javax.inject.Singleton
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

@Singleton
class ValidPixKeyValidador:ConstraintValidator<ValidPixKey, NovaChavePix> {

    override fun isValid(value: NovaChavePix?, context: ConstraintValidatorContext?): Boolean {
        if (value?.tipo == null){
            return false
        }
        return value.tipo.valida(value.chave!!)
    }

}

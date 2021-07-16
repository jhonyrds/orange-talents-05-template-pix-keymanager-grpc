package br.com.zup.pix.util

import br.com.zup.pix.registra.NovaChavePix
import br.com.zup.pix.registra.TipoDeChave
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton

@Singleton
class ValidPixKeyValidador: ConstraintValidator<ValidPixKey,NovaChavePix>{


    override fun isValid(
        value: NovaChavePix,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {

        if(value.tipo == null) {
            return false
        }


        if(value.tipo == TipoDeChave.ALEATORIA) return true


        return value.tipo.validaChave(value.chave!!)
    }
}

package com.app.signme.dataclasses

data class ValidationResult(var failType : Int? = null, var failedViewId : Int? = null)

fun createValidationResult(failType: Int? = null, failedViewId: Int? = null): ValidationResult{
   return ValidationResult(failType = failType, failedViewId = failedViewId)
}
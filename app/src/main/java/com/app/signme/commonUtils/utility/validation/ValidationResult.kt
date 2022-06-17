package com.app.signme.commonUtils.utility.validation

import com.app.signme.dataclasses.ValidationResult

/**
 * Created by HB on 27/6/19.
 */

//data class ValidationResult(var failType: Int? = null, var failedViewId: Int? = null)

data class ValidationMultipleResult(val validationEvent: ArrayList<ValidationResult>)
data class ValidationSingleResult(val validationEvent: ValidationResult)

/**
 * This will return validation object to show validation messages
 * @param failType Int?  This is type of failure like Email Empty etc.
 * These types are defined in [com.app.signme.utility.validation.ValidationResult]
 *
 * @param failedViewId Int? This is the id of view, for which validation is failed and need to focus on that view.
 *
 * @return ValidationResult
 */
fun createValidationResult(failType: Int?= null, failedViewId: Int? = null): com.app.signme.dataclasses.ValidationResult? {
     return ValidationResult(failType = failType, failedViewId = failedViewId)
}
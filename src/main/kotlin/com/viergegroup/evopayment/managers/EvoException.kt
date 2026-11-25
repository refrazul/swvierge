package com.viergegroup.evopayment.managers

/**
 * Excepción personalizada para errores relacionados con el proceso de pago con Evo.
 */
class EvoException : RuntimeException {

    constructor() : super()

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    constructor(cause: Throwable) : super(cause)
}

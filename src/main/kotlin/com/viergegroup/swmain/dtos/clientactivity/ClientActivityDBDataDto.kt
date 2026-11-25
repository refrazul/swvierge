package com.viergegroup.swmain.dtos.clientactivity

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

class ClientActivityDBDataDto {

    var clientActivityId: Long = 0

    var isBlocked: Boolean = false
    var clientIP: String = ""
    var clientEmail: String = ""
    var clientUserName: String = ""
    var clientGeolocation: String = ""
    var deviceModel: String = ""
    var terminalSerialNumber: String = ""


    // Atributos adicionales
    var failedLoginAttempts: Int = 0                // Número de intentos fallidos de inicio de sesión
    var accountLocked: Boolean = false              // Indica si la cuenta está bloqueada
    var suspiciousActivityDetected: Boolean = false // Indica si se detectó actividad sospechosa
    var accountSuspicious: Boolean = false          // Indica si la cuenta es sospechosa
    var reasonForBlocking: String = ""              // Razón específica para el bloqueo



    var lastLoginDateTime: LocalDateTime? = null    // Última vez que el usuario inició sesión
    var registrationDateTime: LocalDateTime? = null // Fecha y hora de registro del usuario
    var numberOfTransactions: Int = 0               // Número de transacciones realizadas
    var numberOfRejectedTransactions: Int = 0               // Número de transacciones realizadas

    var totalAmountSpent: Double = 0.0              // Monto total gastado por el usuario
    var frequentLocation: String = ""               // Ubicación frecuente del usuario
    var numberOfDevices: Int = 0                    // Número de dispositivos usados por el usuario

    var clonnedRequests: Int = 0

    var lastTransactionDateTime: LocalDateTime? = null // Fecha y hora de la última transacción
    var lastModificationDateTime:String=""
    var creationDateTime:String=""

}
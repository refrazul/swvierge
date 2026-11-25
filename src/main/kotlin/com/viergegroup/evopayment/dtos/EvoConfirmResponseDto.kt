package com.viergegroup.evopayment.dtos

class EvoConfirmResponseDto {
    var ConfirmResponse: EvoConfirmResponseBody = EvoConfirmResponseBody()
}

class EvoConfirmResponseBody {
    var ResponseCode: String = ""              // "00" = Aprobado
    var ResponseMessage: String = ""           // Mensaje textual, ej: "APPROVED"
    var AnswerKey: String = ""                 // Clave única asignada a esta Confirm
    var CompanyIdentification: String = ""
    var SystemIdentification: String = ""
    var BranchIdentification: String = ""
    var PosIdentification: String = ""
    var Reference: String = ""                 // Referencia local
}
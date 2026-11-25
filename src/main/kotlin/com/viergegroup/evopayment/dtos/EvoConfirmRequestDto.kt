package com.viergegroup.evopayment.dtos

class EvoConfirmRequestDto {
    var Confirm: EvoConfirmDto = EvoConfirmDto()
}

class EvoConfirmDto {
    var RequestType: String = "Confirm"
    var Reference: String = ""                 // Folio o ID de tu POS
  //  var AnswerKey: String = ""                 // El AnswerKey recibido de la venta
    var CompanyIdentification: String = "1000001"
    var SystemIdentification: String = "TPV_1.0.0"
    var BranchIdentification: String = "1100001"
    var POSIdentification: String = "1110001"
}

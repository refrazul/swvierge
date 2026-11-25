
package com.viergegroup.evopayment.dtos

class EvoPaymentResponseDto {
    var SaleResponse: SaleResponse = SaleResponse()
}
class SaleResponse {
    var TransactionFacilityPayments: Int = 0
    var TransactionFacilityType: String = ""
    var Plan: Plan = Plan()
    var PlanDescription: String = ""
    var PaymentMethodId: Int = 0
    var PaymentMethodDescription: String = ""
    var CardDescription: String = ""
    var CardNumber: String = ""
    var CardHashing: String = ""
    var CardReadMode: String = ""
    var CardReadModeDescription: String = ""
    var CardTypeDescription: String = ""
    var MerchantID: String = ""
    var TerminalID: String = ""
    var TerminalTrace: Int = 0
    var SettlementBatchNumber: Int = 0
    var MerchantCategory: MerchantCategory = MerchantCategory()
    var CardCategory: CardCategory = CardCategory()
    var TransactionDescription: String = ""
    var CurrencySymbol: String = ""
    var CurrencyDescription: String = ""
    var HostID: Int = 0
    var TransactionResolutionMode: String = ""
    var AuthHostMsgType: String = ""
    var HostMsgType: String = ""
    var ValueFacilityPayments: Double = 0.0
    var TransmitionDateTime: String = ""
    var AuthResultCode: String = ""
    var AuthHostProcessCode: String = ""
    var AuthCode: String = ""
    var AuthRRN: String = ""
    var AuthTicket: Int = 0
    var AuthDateTime: String = ""
    var ResponseActions: List<String> = emptyList()
    var ResponseMessage: String = ""
    var ResponseCode: Int = -1
    var TaxFinancialCostAmount: String = ""
    var TransactionRequestAmount: Double = 0.0
    var TransactionAuthenticationType: String = ""
    var RequiredInformation: List<RequiredInformation> = emptyList()
    var Sequence: String = ""
    var Configuration: Configuration = Configuration()
    var AnswerKey: String = ""
    var PublicAnswerKey: String = ""
    var Tickets: List<Any> = emptyList()
    var CompanyIdentification: String = ""
    var SystemIdentification: String = ""
    var BranchIdentification: String = ""
    var POSIdentification: String = ""
    var CurrencyCode: Int = 0
    var Amount: Double = 0.0
    var Reference: String = ""
}
class Plan {
    var Id: String = ""
    var ForeignIdentifier: String = ""
    var Host: Host = Host()
    var Description: String = ""
    var FacilityPayments: Int = 0
    var FacilityType: String = ""
    var Currency: Int = 0
    var Rate: Int = 0
    var IsDynamic: Boolean = false
    var PosOrDeviceActions: List<String> = emptyList()
    var PosOrDeviceOptions: String = ""
    var Category: Int = 0
    var Cashback: Cashback = Cashback()
    var Deferral: Deferral = Deferral()
}

class Host {
    var Id: Int = 0
}

class Cashback {
    var IsAllowed: String = ""
    var MinPercAmount: Double = 0.0
    var MaxPercAmount: Double = 0.0
}

class Deferral {
    var IsAllowed: String = ""
}

class MerchantCategory {
    var Code: Int = 0
}

class CardCategory {
    var Name: String = ""
    var ForeignIdentifier: String = ""
}

class RequiredInformation {
    var Name: String = ""
    var Value: String = ""
}

class Configuration {
    var Company: Company = Company()
    var Branch: Branch = Branch()
    var System: System = System()
}

class Company {
    var Id: String = ""
    var Name: String = ""
    var Description: String = ""
    var TaxCode: String = ""
    var Address: String = ""
    var City: String = ""
    var State: String? = null
    var RegionCode: String = ""
    var ZipCode: String? = null
    var CountryCode: String = ""
    var Category: Int? = null
    var AdditionalConfig: String = ""
    var Label: String? = null
}

class Branch {
    var Id: String = ""
    var Name: String = ""
    var ForeignIdentifier: String = ""
    var Description: String = ""
    var Address: String = ""
    var City: String = ""
    var State: String = ""
    var RegionCode: String = ""
    var ZipCode: String = ""
    var CountryCode: String = ""
    var Category: Int = 0
}

class System {
    var Id: String = ""
    var Name: String = ""
    var Description: String = ""
    var AdditionalConfig: AdditionalConfig = AdditionalConfig()
}

class AdditionalConfig {
    var VoidIsReverse: Int = 0
}

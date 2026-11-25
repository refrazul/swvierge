package com.viergegroup.swmain.entities.catalogs
enum class HostType(var hostId: Int, var hostTypeName: String, var vaultName: String, var vaultType: String, var authorizer: Authorizer = Authorizer.Undefined, val color: String,val usesIso8583:Boolean=true) {
    Undefined(-1, "Undefined", "undefined","", Authorizer.Undefined, "\u001B[97m"),  // Blanco brillante
    ProsaDUKPT(0, "ProsaDUKPT", "prosa","PROD", Authorizer.PROSA, "\u001B[96m"),  // Azul brillante 
    ProsaTLS(1, "ProsaTLS", "prosa","PROD", Authorizer.PROSA, "\u001B[96m"),  // Verde brillante 
    TestingDUKPTHost(2, "TestingDUKPTHost", "prosa","DEV", Authorizer.PROSA, "\u001B[96m"),  // Cian brillante
    TestingTLSHost(3, "TestingTLSHost", "prosa","DEV", Authorizer.PROSA, "\u001B[95m"),  // Magenta brillante
    TestingBanorte(4, "TestingBanorte", "banorte","DEV", Authorizer.BANORTE, "\u001B[91m"),  // Rojo brillante
    Banorte(5, "Banorte", "banorte","PROD", Authorizer.BANORTE, "\u001B[93m"),  // Amarillo brillante
    ProsaDUKPTDev(6, "ProsaDUKPTDev", "banorte","PROD", Authorizer.PROSA, "\u001B[94m"),  // Azul brillante
    ProsaDUKPTProd(7, "ProsaDUKPTProd", "banorte","PROD", Authorizer.PROSA, "\u001B[92m"),  // Verde brillante
    OpenPayDev(8, "OpenPayDev", "openpay","DEV", Authorizer.OPENPAY, "\u001B[96m"),  // Cian brillante
    OpenPayProd(9, "OpenPayProd", "openpay","PROD", Authorizer.OPENPAY, "\u001B[95m"),  // Magenta brillante
    VelPayDev(10, "VelPayDev", "velpaydev","DEV", Authorizer.VELPAY, "\u001B[91m"),
    Vierge(11, "vierge", "vierge","DEV", Authorizer.VIERGE, "\u001B[93m"),  // Amarillo brillante 
    SecretsVierge(12, "secrets_vierge", "vierge","PROD", Authorizer.OPENPAY, "\u001B[97m"),  // Blanco brillante
    ViergeTestDev(13, "ViergeTestDev", "vierge","DEV", Authorizer.VIERGE, "\u001B[94m"),  // Azul brillante
    ViergeTestProd(14, "ViergeTestProd", "vierge","PROD", Authorizer.VIERGE, "\u001B[92m"),  // Verde brillante 
    Testing(15, "ViergeTestDev", "vierge","DEV", Authorizer.VIERGE, "\u001B[96m"),  // Cian brillante
    YoLoPago(16, "YoLoPago", "vierge","", Authorizer.YOLOPAGO, "\u001B[96m"),  // Cian brillante
    PaynetDev(17, "PaynetDev", "paynet","", Authorizer.PAYNET, "\u001B[96m"),  // Cian brillante
    PaynetProd(18, "PaynetProd", "paynet","", Authorizer.PAYNET, "\u001B[96m") , // Cian brillante
    ClaroPay(19, "ClaroPay", "claropay","", Authorizer.CLAROPAY, "\u001B[96m") ,
    CrediBancoDev(20, "CrediBancoDev", "credibanco","DEV", Authorizer.CREDIBANCO, "\u001B[94m"), // Azul brillante
     CrediBancoProd(21, "CrediBancoProd", "credibanco","PROD", Authorizer.CREDIBANCO, "\u001B[94m"), // Azul brillante

    EvoPaymentDev(22, "EvoPaymentDev", "evopayment","DEV", Authorizer.EVOPAYMENT, "\u001B[94m",false),
    EvoPaymentProd(23, "EvoPaymentProd", "evopayment","PROD", Authorizer.EVOPAYMENT, "\u001B[94m",false),
    ClaroPayT2P(24, "ClaroPayT2P", "claropay","PROD", Authorizer.CLAROPAY, "\u001B[96m")  // Cian brillante
    
     //yolopago      
    ;    
    


    companion object{
        private val map = values().associateBy(
            HostType::hostTypeName)
        fun fromName(hostTypeName: String): HostType {
            return map[hostTypeName] ?: return Undefined
        }
        fun getVaultNames(): MutableList<String> {
            return HostType.values().map { it.vaultName }.toMutableList()
        }

        
        private val map2 = values().associateBy(
            HostType::hostId)
        fun fromId(hostId: Int) = map2[hostId]

        var types= mutableListOf<String>()

        fun getNames(): MutableList<String>{
            if(types.size==0) {
                values().forEach {
                    types.add(it.name)
                }
            }

            return types
        }
    }

}
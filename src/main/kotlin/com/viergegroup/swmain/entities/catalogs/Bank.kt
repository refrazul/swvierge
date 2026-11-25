package com.viergegroup.swmain.entities.catalogs


enum class Bank(var bankId:Int,var bankName:String,var rsaId:String){
    Undefined(-1, "Undefined", ""),
    Afirme(1, "Afirme", "A000OPFT41"),
    BancoAzteca(2, "Banco Azteca", "A000OPFT42"),
    Inbursa(3, "Inbursa", "A000OPFT43"),
    Mifel(4, "Mifel", "A000OPFT44"),
    BanBajio(5, "BanBajio", "A000OPFT46"),
    Banorte(6, "Banorte", "A000OPFT47"),
    BanRegio(7, "BanRegio", "A000OPFT48"),
    BancoDelBajio(8, "Banco del Bajío", "A000OPFT49"),
    Citibanamex(9, "Citibanamex", "A000OPFT50"),
    HSBC(10, "HSBC", "A000OPFT51"),
    Invex(11, "Invex", "A000OPFT52"),
    Ixe(12, "Ixe", "A000OPFT53"),
    Santander(13, "Santander", "A000OPFT54"),
    Scotiabank(14, "Scotiabank", "A000OPFT55"),
    BancoFamsa(15, "Banco Famsa", "A000OPFT56"),
    BancoMultiva(16, "Banco Multiva", "A000OPFT57"),
    BancoFinterra(17, "Banco Finterra", "A000OPFT58"),
    Bansefi(18, "Bansefi", "A000OPFT59"),
    BancoCompartamos(19, "Banco Compartamos", "A000OPFT60"),
    Bancoppel(20, "Bancoppel", "A000OPFT61"),
    BancaMifel(21, "Banca Mifel", "A000OPFT62"),
    Consubanco(22, "Consubanco", "A000OPFT63"),
    DeutscheBank(23, "Deutsche Bank", "A000OPFT64"),
    Monex(24, "Monex", "A000OPFT65"),
    BancoPagaTodo(25, "Banco PagaTodo", "A000OPFT66"),
    PrudentialBank(26, "Prudential Bank", "A000OPFT67"),
    Autofin(27, "Autofin", "A000OPFT68"),
    Actinver(28, "Actinver", "A000OPFT69"),
    Intercam(29, "Intercam", "A000OPFT70"),
    Bansi(30, "Bansi", "A000OPFT71"),
    BancoSab(31, "Banco Sabadell", "A000OPFT72"),
    Bankaool(32, "Bankaool", "A000OPFT73"),
    BankOfAmerica(33, "Bank of America", "A000OPFT74");

   ;
    ;
    // CheckIn(51,"CheckIn");


    companion object{
        private val map = Bank.values().associateBy(
            Bank::bankName)
        fun fromName(bankName: String) = map[bankName]

        private val map2 = values().associateBy { it.bankName.toUpperCase() }

        fun fromNameSuper(bankName: String): Bank {
            // Normalizar el nombre del banco a mayúsculas
            val normalizedBankName = bankName.toUpperCase()
    
            // Buscar directamente el nombre normalizado
            map2[normalizedBankName]?.let { return it }
    
            // Búsqueda más profunda por abreviaciones comunes o nombres parciales
            return values().find { bank ->
                normalizedBankName in bank.bankName.toUpperCase() ||
                bank.bankName.toUpperCase().contains(normalizedBankName) ||
                normalizedBankName in bank.rsaId
            } ?: Undefined
        }     
 



    }

}
package com.viergegroup.isoprocessor.models.tokens

enum class TokenId (var tokenIdString:String,var tokenDescription:String){
    Undefined("",""),

    Token00("00",""),
    Token04("04",""),

    Q1("Q1",""), 
    Q2("Q2",""),
    Q6("Q6",""),  

    ES("ES",""),
    EZ("EZ",""),
    B2("B2",""),
    B1("B1",""),
    B3("B3",""),
    B4("B4",""),
    B5("B5",""),
    B6("B6",""),
    BD("BD",""),
    BY("BY",""),

    ER("ER",""),
    EX("EX",""),
    EW("EW",""),
    CZ("CZ",""),
    C0("C0",""),
    C6("C6",""),
    CE("CE",""),
    CH("CH",""), 

    C4("C4",""); 
    companion object{
        private val map = TokenId.values().associateBy(
            TokenId::tokenIdString)
        fun fromTokenIdString(tokenIdString: String):TokenId {
            if(map[tokenIdString]==null) return Undefined
            return  map[tokenIdString]!!
        }
    }

}
package com.viergegroup.swmain.emvparserneo


enum class EmvTag(var emvTagId: Int, var emvTagStr: String = "", var emvTagName: String = "", var isConstructed:Boolean=false) {



    Unknown(0, "0", "Unknown"),

    Tag6F(0x6F, "6F", " File Control Information (FCI) Template",true),
    Tag84(0x84, "84", "Dedicated File (DF) Name",true),
    TagBF0C (0xBF0C , "BF0C", "File Control Information (FCI) Issuer Discretionary Data",true),
    Tag61 (0x61  , "61", "Application Template",true),
    TagA5  (0xA5  , "A5", "File Control Information (FCI) Proprietary Template",true),
    Tag77  (0x77  , "77", "Response Message Template Format 2",true),
    Tag82  (0x82  , "82", " Application Interchange Profile",true),
    Tag70  (0x70  , "70", " EMV Proprietary Template",true),

    Tag8C  (0x5F25  , "5F25", "Card Risk Management Data Object List 1 (CDOL1)"),
    Tag8D   (0x8D   , "8D", " Card Risk Management Data Object List 2 (CDOL2)"),
    Tag9F08    (0x9F08     , "9F08", "  Application Version Number"),
    Tag9F32   (0x9F32     , "9F32", "  Issuer Public Key Exponent"),
    Tag8F    (0x8F      , "8F", "   Certification Authority Public Key Index"),

    Tag5F55 (0x5F55 , "5F55", "Issuer Country Code (alpha2 format)"),
    Tag94 (0x94 , "94", "Application File Locator (AFL)"),
    Tag42  (0x42  , "42", "Issuer Identification Number (IIN)"),
    Tag90(0x90, "90", "Issuer Public Key Certificate"),
    Tag4F(0x4F, "4F", "Application Identifier (AID)"),
    Tag50(0x50, "50", "Application Label"),
    Tag57(0x57, "57", "Track 2 Equivalent Data"),
    Tag5A(0x5A, "5A", "Application Primary Account Number (PAN)"),
    Tag5F20(0x5F20, "5F20", "Cardholder Name"),
    Tag5F24(0x5F24, "5F24", "Application Expiration Date"),
    Tag5F25(0x5F25, "5F25", "Application Effective Date"),
    Tag5F28(0x5F28, "5F28", "Issuer Country Code"),
    Tag5F2A(0x5F2A, "5F2A", "Transaction Currency Code"),
    Tag5F30(0x5F30, "5F30", "Service Code"),
    Tag5F34(0x5F34, "5F34", "Application PAN Sequence Number"),
    Tag87(0x87, "87", "Application Priority Indicator"),
    Tag8A(0x8A, "8A", "Authorization Response Code"),
    Tag8E(0x8E, "8E", "Cardholder Verification Method (CVM) List"),
    Tag95(0x95, "95", "Terminal Verification Results (TVR)"),
    Tag9A(0x9A, "9A", "Transaction Date"),
    Tag9B(0x9B, "9B", "Transaction Status Information"),
    Tag9C(0x9C, "9C", "Transaction Type"),
    Tag9F02(0x9F02, "9F02", "Amount, Authorised (Numeric)"),
    Tag9F03(0x9F03, "9F03", "Amount, Other (Numeric)"),
    Tag9F06(0x9F06, "9F06", "Application Identifier (AID) - Terminal"),
    Tag9F07(0x9F07, "9F07", "Application Usage Control"),
    Tag9F0D(0x9F0D, "9F0D", "Issuer Action Code - Default"),
    Tag9F0E(0x9F0E, "9F0E", "Issuer Action Code - Denial"),
    Tag9F0F(0x9F0F, "9F0F", "Issuer Action Code - Online"),
    Tag9F10(0x9F10, "9F10", "Issuer Application Data"),
    Tag9F1A(0x9F1A, "9F1A", "Terminal Country Code"),
    Tag9F26(0x9F26, "9F26", "ARQC"),
    Tag9F27(0x9F27, "9F27", "Cryptogram Information Data"),
    Tag9F34(0x9F34, "9F34", "Indicates the results of the last CVM performed"),
    Tag9F66 (0x9F34, "9F66 ", "TTQ (Terminal Transaction Qualifiers) Decoder"),
    Tag9F36(0x9F36, "9F36", "Application Transaction Counter (ATC)"),
    Tag9F37(0x9F37, "9F37", "Value to provide variability and uniqueness to the generation of a cryptogram\t"),
    Tag9F38(0x9F38, "9F38", "PDOL"),
    Tag9F46 (0x9F34, "9F46", "ICC Public Key certified by the issuer"),
    Tag9F1E(0x9F1E, "9F1E", "Interface Device (IFD) Serial Number");

    companion object{

        private val map = EmvTag.values().associateBy(
            EmvTag::emvTagStr)
        fun fromTagStr(tag: String):EmvTag {
            return map[tag]?:Unknown
        }

    }


}
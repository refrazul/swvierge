package com.shared.commons

import com.viergegroup.isoprocessor.models.descriptors.Iso8583FieldDescriptor
import com.viergegroup.isoprocessor.models.descriptors.TokenFieldDescriptor
import com.viergegroup.swmain.entities.catalogs.HostType
import com.viergegroup.swmain.entities.transactions.TransactionData

import com.viergegroup.swmain.webwatcher.Watcher
  

enum class ErrorCode(var errorCodeInt:Int=0,var responseCode:String="00",var errorCodeMessage:String="",var mustSendAReverse:Boolean=false){
    Undefined(-1,"","Undefined Error Code"),
   
    Approved(0,"00",""),  
    
     
    ///////////////////////////////// /////////////////////////// ISO STANDAR ERRORS
    ReferToCardIsuer(1,"01","Refer to card issuer, CARD ISSUER ERROR "),
    ReferToCardIsuerSpecialCondition(2,"02","Refer to card issuer's special conditions\n "),
    InvalidMerchant(3,"03","Invalid merchant "), 
    PickUp(4,"04","Pick-up"),
    DoNotHonor(5,"05","Do not honor\n"),
    Error(6,"06","Generic Error 06 \n"), 

    PickUpCardSpecialCondition(7,"07","Pick-up card, special condition\n"),
    HonourWithIdentification(8,"08","Honour with identification\n"),
    TransactionDuplicated(9,"09","Transaction Duplicated\n"),

    ApprovedForPartialAmount(10,"10","Approved for partial amount\n"),
    ApprovedVIP(11,"11","Approved  VIP"),
    InvalidTransaction(12,"12","Invalid Transaction or Card"),
    InvalidAmount(13,"13","Invalid Amount\n"),
    InvalidCardNumber(14,"14","Invalid Card Number"),

    NoSuchIssuer(15,"15","No such issuer\n"),

    
    ApprovedUpdateTrack3(16,"16","Approved, update track 3\n"),
    CustomerCancellation(17,"17","Customer cancellation\n"),
    CustomerDispute(18,"18","Customer dispute\n"),
    ReenterTransaction(19,"19","Re-enter transaction\n"),
    InvalidResponse(20,"20","Invalid response\n"),
    NoActionTaken(21,"21","No action taken"),
    SuspectedMalfunction(22,"22","Suspected malfunction\n"),
    UnacceptableTransactionFee(23,"23","Unacceptable transaction fee\n"),
    FileUpdateNotSupportedByReceiver(24,"24","File update not supported by receiver\n"),
    UnableToLocateRecordOnFile(25,"25","Unable to locate record on file\n"),
    DuplicateFileUpdateRecord(26,"26","Duplicate file update record, old record replaced\n"),
    FileUpdateFieldEditError(27,"27","File update field edit error\n"),
     FileUnavailable(28,"28","File Unavailable For Update"),

   
    InvalidFormat(30,"30","Format error, VERIFICAR TAGS: 57, 9F10, 9F09, 9F26, 9F34, or field 60 ,validar la llave de encripcion DUKPT"),
    BankNotSupportedBySwitch(31, "31", "Bank not supported by switch"),
    CardExpired33(33, "33", "CardExpired 33"),
    SuspectedFraud34(34, "34", "SuspectedFraud 34 "),
    CardAcceptorCallAcquirerSecurity(35, "35", "Card Acceptor Call Acquirer Security  "),
    RestrictedCard36(36, "36", "Restricted Card 36  "),

    CardAcceptCallSecurity(37, "37", "Card acceptor call acquirer security"),
    AllowablePINTriesExceeded38(38, "38", "Allowable PIN Tries Exceeded 38  "),
    NoCreditAccount(39, "39", "No Credit Account  "),

    RequestedFunctionNotSupported(40, "40", "Requested Function Not Supported "),

    CardLostOrStolen(41, "41", "Card reported lost or stolen"),
    CardReportedStolen(43, "43", "Card reported stolen"),
    ClosedAccount(46, "46", "Closed account"),

    InsufficientFunds(51, "51", "Insufficient funds"),
    NoCheckingAccount(52, "52", "No checking account"),
    NoSavingsAccount(53, "53", "No savings account"),
    CardExpired(54, "54", "Card expired"),

    InvalidPIN(55, "55", "Incorrect PIN"),
    NoCardRecord(56, "56", "No card record"),

    

    TransactionNotAllowed(57, "57", "Cardholder transaction not permitted"),
    TerminalTransactionNotAllowed(58, "58", "Terminal transaction not permitted"),
    SuspectedFraud(59, "59", "Suspected fraud"),
    ActivityAmountLimitExceeded(61, "61", "Activity amount limit exceeded"),
    RestrictedCard(62, "62", "Restricted card (e.g., country exclusion list)"),
    SecurityViolation(63, "63", "Security violation"),

    OriginalAmountIncorrect(64, "64", "Original amount incorrect"),
    ActivityCountLimitExceeded(65, "65", "Activity count limit exceeded"),
 
    KeyLoadIsNecesary(70,"70","Key Load Is Necessary"),  

      PINDataRequired(0X1027,"Z7","PINDataRequired-70"),

    PINNotChanged(71,"71"," PINNotChanged"),
 
    RsaFailed(72,"72"," Key Load Failed  ( Incorrect RSA )"),
    PINEncryptError(74,"74"," PIN Encrypt Error"),

    AllowablePINTriesExceeded(75, "75", "Allowable number of PIN tries exceeded"),
  
    InvalidAccount(76, "76", "Invalid account"),
    NoSharingBetweenIssuerAndTerminal(77, "77", "No sharing between card issuer and terminal owner"),
   
     ReversedLifeCycle(79, "79", "ReversedLifeCycle"),
    NoFinancialImpact(80, "80", "NoFinancialImpact"), 

   
    CryptographicErrorPIN(81, "81", "Cryptographic error found in PIN"),

    CardNotActivated(78, "78", "Blocked, first use. The transaction is from a new cardholder, but the card was not properly activated."),
    NegativeCAM(82, "82", "Negative CAM, dCVV, iCVV, or CVV results"),
     FraudSecurity(83, "83", "FraudSecurity"),

     NoBFUpdateErrorNoreasonToDecline(84, "84", "PrivateUse:NoBFUpdateError-NoreasonToDecline"),


 
    NoReasonToDecline(85, "85", "No reason to decline"),

     InvalidAuthTypeOrCannotVerifyPin(86, "86", "InvalidAuthType or cannot verify pin"),
     BadTrackDataOrCashbackNA(87, "87", "BadTrackData or PurchaseAmount only no cashback allowed"),
     PTLFErrorOrCryptoError(88, "88", "BadTrPTLFErrorOrCryptoErrorackData or PurchaseAmount only no cashback allowed"),
     InvalidRouteServiceOrPinError(89, "89", "InvalidRouteServiceOrPinError"),

     CutOffInProcessess(90, "90", "CutOffInProcessess"),

    IssuerUnavailableOrInoperative(91, "91", "Service unavailable"),   
    RoutingDestinationNotFound(92, "92", "Routing destination not found"),
    LawViolation(93, "93", "Cannot complete transaction due to law violation"),
    DuplicateTransmission(94, "94", "Duplicate transmission"),
    ReconciliationError(95, "95", "Reconciliation error"),
    SystemMalfunction(96, "96", "System malfunction or certain field error conditions"),
    AdditionalCustomerAuthentication(0x1A00, "1A", "Additional customer authentication required"),

 

    CardAuthenticationFailed(101,"Q1","CardAuthenticationFailed"),
   // InfoTxnDataIncorrect(102,"U7","InfoTxnData Incorrect"),

    HostErrorFromProsaAuthorizatorUnreachable(103,"T2","HostError From Prosa - NOT SURE: Authorizator Unreachable "),
    DuplicateTransaction(104,"T9","Duplicate Transaction "),
    UndefinedT5(108,"T5","T5 undefined"),
    UndefinedT6(109,"T6","T6  undefined"),
     
    GeneralError(107,"T3","T3 Means: Error Validating EMV Data, Auth Error or  System Error"),

   ErrorBuildingIso(110,"I0","ErrorBuildingIso"),

    ErrorParsingIso(111,"I1","ErrorParsingIso"),
 


    //////////////////////////////////////////////////////////// Reversos
    SuspiciousReversal(0x0100,"S1","  Suspicious reversal override"),
    Misdispensereversal(0x0101,"S2","  Misdispense reversal override"),

    
    //////////////////////////////////////////////////////////// FORMATS
    HostIsNotAvailable(0x0200,"F0"," Host Is Not Available"),
    InvalidIso8583(0x0201,"F1","INVALID Iso Format "),
    FieldIsMandatory(0X0202,"F2","Field Is Mandatory"),
    InvalidFormatField(0X0203,"F3","Invalid Field Format  "),
    InvalidTokenFormant(0X0204,"F4","Invalid Token Formant  "),
    IsoIsBadFormedOrIsNull(0X0205,"F5","Iso Is BadFormed Or Is Null"),
 

    RSADOesntExists(0X0206,"F6", "RSA Doesnt Exists  "),
    Track2IsMandatory(0X0207,"F7", "Track2 Is Mandatory "),
    InvalidScriptExpression(0X0209,"F9", "Invalid Script Expression  "),
    SystemTraceNumberIsNecesary(0X020A,"FA", "System Trace Number Is Necesary  "),
    EMVDataFormatError(0X020B,"FB", "EMV Data Format Error "),



    ///////////////////////////////// DIFFERENT CONDITIONS 





    //////////////////////////////////////////////////////////// HOST ERRORS


    EmptyResponce(0X0109,"H1", "Empty Responce (0 Bytes Received From Host) "),
    HostIsInactive(0X0102,"H2"," HOST IS INACTIVE "),
    HostIsUnknown(0X0103,"H3"," HOST Is NOT VALID "),
    HostError(0X0104,"H4","HOST DIDN'T Answer Correctly "),
    HostTimeOut(0X0105,"68", "HostTime Out ",true),  
     
    HostDisconnected(0X0106,"H6", "Host Disconnected  "),
    HostResponseInvalidFormat(0X0107,"H8", "Host Response Message Invalid Format "),
    HostConnectionError(0X0108,"H7", "Host Connection Error "),
    ClientTimeOut(0X010A,"H9", "Client Time Out ",true),  
     ReverseFailed(0X010B,"HA", "Reverse Failed"),  // NO SE DEBE REINTENTAR REVERSO
    ReverseTimeOut(0X010C,"HB", "Reverse Time Out"), // SE DEBE REINTENTAR REVERSO 
    TxnFailed(0X010D,"HC", "Txn Failed"),    
      CommError(0X010E,"HE", "Comm Error, There are errors in Channel"),    
      ISOBufferError(0X010F,"HF", "ISOBufferError "),    

  
    //////////////////////////////////////////////////////////// DB - ADVICE


    TransactionDoesntExist(0X0301,"D1", " Transaction Doesnt Exist"),
    OriginalTransactionDoesntExist(0X0302,"D2", "Original Transaction Doesnt Exist"),
    AdviceTransactionCantGenerateAdviceTransaction(0X0303,"D3", "Advice Transaction Cant Generate Advice Transaction"),
    OriginalTransactionWasCancelled(0X0304,"D4", "Original Transaction Was Cancelled"),
    OriginalTransactionWasRefunded(0X0305,"D5", "Original Transaction Was Refunded"),
    CantBeReversed(0X0306,"D6", " Cant Be Reversed"), 

    //////////////////////////////////////////////////////////// VAULT - HSM


    TokeEXIsEmpty(0X0901,"E1","TokeEX Is Empty"),
    TokeEXBadFormat(0X0902,"E2","TokeEX Bad Format"),
    SerialIsEmpty(0X0903,"E3","Serial Number Is Mandaotry and Must Have Len of 20"),
    KeyIdDoesntExist(0X0904,"E4","KeyId Doesnt Exist"),
    TokeEWBadFormat(0X090E,"E5","TokeEw Bad Format"),
    TokeEPBadFormat(0X090F,"E6","TokeEw Bad Format"),




    HSMError(0X0905,"X5","HSM Error"),
    CRC32Error(0X0906,"X6","CRC32 Error"),
    InvalidHVOperation(0X0907,"X7","Invalid HSM Or Vault Operation, MUST DO C-X-F"),
    TKFailed(0X0908,"X8","TK Failed"),
    VaultError(0X0909,"X9","Vault Error"),
    SecurityError(0X090A,"XA","Security Error"),
    RSAHashError(0X090B,"XB","Error Calculating RSA Hash"),
    CredentialsNotFound(0X090C,"XC","Credentials Not Found"),
    KeyLoadPinIsNecesary(0X090D,"XD","KeyLoad Pin Is Necesary"),
 
    //////////////////////////////////////////////////////////// OTHER
    AuthError(0X1001,"A1", "Auth Exception"),



    CryptogramVerificationFailure(0X1002, "U0", "Cryptogram verification failure decline"),
    SecurityModuleParameterError(0X1003, "U1", "Security module parameter error"),
    SecurityModuleFailure(0X1004, "U2", "Security module failure"),
    Key1RecordNotFound(0X1005, "U3", "KEY1 record not found"),
    ATCCheckFailure(0X1006, "U4", "ATC check failure"),
    CVRDecline(0X1007, "U5", "CVR decline"),
    TVRDecline(0X1008, "U6", "TVR decline"),
    ReasonOnlineCodeDecline(0X1009, "U7", "Reason online code decline"),
    FallbackDecline(0X100A, "U8", "Fallback decline"),

    CryptogramVerificationReferral(0X100B, "V0", "Cryptogram verification failure referral"),
    CVRReferral(0X100C, "V1", "CVR referral"),
    TVRReferral(0X100D, "V2", "TVR referral"),
    OnlineCodeReferral(0X100E, "V3", "Reason online code referral"),
    FallbackReferral(0X100F, "V4", "Fallback referral"),
 
    CryptogramVerificationCapture(0X1010, "V7", "Cryptogram verification failure capture"),
    CVRCapture(0X1011, "V8", "CVR capture"),
    TVRCapture(0X1012, "V9", "TVR capture"),

    UnableToAuthorize(0X1013, "N0", "Unable To Authorize"),
    VerificationDataFailed(0X1014, "6P", "VerificationDataFailed"),
    SsubchargeAmountNotPermitted(0X1015, "B1", "SsubchargeAmountNotPermitted"),
    CashServiceNotAvailable(0X1016, "N3", "CashServiceNotAvailable"),
    CashRequestExceedsIssuerLimit(0X1017, "N4", "CashRequestExceedsIssuerLimit"),
    DecilneForCVV2(0X1018, "N7", "DecilneForCVV2"),
    AmountExceedsPreAuthorized(0X1019, "N8", "AmountExceedsPreAuthorized"),
    DeniedPinUnblock(0X101A, "P5", "DeniedPinUnblock"),
    DeniedPinChange(0X101B, "P6", "DeniedPinChange"),
    StopPaymentOrder(0X101C, "R0", "StopPaymentOrder"),
    RevocationAuthOrder(0X101D ,"R1", "RevocationAuthOrder"),
    TxnDoesNotQualify(0X101E ,"R2", "TxnDOesNotQualify"),
    RevocationAuthOrder02(0X101F ,"R3", "RevocationAuthOrder02"),
       DestinationNotAvailable(0X1023 ,"R9", "DestinationNotAvailable"),
       SuspectReversal(0X1024 ,"S0", "SuspectReversal"),
       AddCashWithdrawal(0X1025 ,"S3", "AddCashWithdrawal"),
 
    UnableToGoOnline(0X1020 ,"Z3", "UnableToGoOnline"),
    AmountNotSupported(0X1021 ,"Z5", "AmountNotSupported"),
    InvalidUseMCC(0X1022,"Z6", "InvalidUseMCC"),



      
    //UndefinedT9(0X2001 ,"T9", "UndefinedT9"),
   // UndefinedN0(0X2002 ,"N0", "UndefinedN0"),
     UndefinedN2(0X2003,"N2", "UndefinedN2"),
  //  UndefinedN3(0X2004,"N3", "UndefinedN3"),
  // UndefinedN4(0X2005,"N4", "UndefinedN4"),
    UndefinedN5(0X2006,"N5", "UndefinedN5"),
    UndefinedP0(0X2007,"P0", "UndefinedP0"), 
    UndefinedP1(0X2008,"P1", "UndefinedP1"),



    UnHandledException(0X0501,"O1", "Unhandled Exception"),  
     // MULTIPAY ERRORES 
    AuthorizationNumberRequired(0X0502,"L1", "Authorization Number Required"),  
  
     AppleApiError(0X0701,"APL1", "Apple Api Error"),
     AppleHSMError(0X0702,"APL2", "Apple HSM Error"), 
 
        // ===== EVO ERROR CODES ===== 
    EvoCardNotRead(0xE3E8, "1000", "Tarjeta no leída"),
    EvoBrandMismatch(0xE3E9, "1001", "Marca enviada no coincidente con utilizada en el GetCard"),
    EvoCardNotConfigured(0xE3EA, "1002", "Tarjeta no configurada"),
    EvoBrandDoesNotSupportInstallments(0xE3EB, "1003", "Marca no admite cuotas"),
    EvoBrandNotConfigured(0xE3EC, "1004", "Marca no configurada"),
    EvoBrandNotConfiguredInBranch(0xE3ED, "1005", "Marca no configurada en sucursal"),
    EvoPlanInstallmentsMismatch(0xE3EE, "1006", "Plan/Cuotas no corresponden con marca"),
    EvoCurrencyCodeNotConfigured(0xE3EF, "1007", "Código moneda en plan no configurado"),
    EvoNodeCodeNotConfigured(0xE3F0, "1008", "Código nodo en sucursal no configurado"),
    EvoCashRegisterWithoutTerminal(0xE3F1, "1009", "Caja sin nro. de terminal configurada"),
    EvoNodeParameterError(0xE3F2, "1010", "Error de configuración de parámetros para el nodo"),
    EvoInvalidCard(0xE3F3, "1011", "Tarjeta inválida"),
    EvoBankConfigurationError(0xE3F4, "1012", "Error de configuración de Bancos"),
    EvoInvalidPlan(0xE3F5, "1013", "Plan no válido"),
    EvoTransactionExpired(0xE3F6, "1014", "Transacción expirada"),
    EvoHostOffline(0xE3F7, "1015", "Host sin conexión"),
    EvoCompanyMismatch(0xE3F8, "1016", "Compañía enviada no coincidente a la usada en el GetCard"),
    EvoSystemNumberMismatch(0xE3F9, "1017", "Nro de sistema enviado no coincidente al usado en el GetCard"),
    EvoBrandNumberMismatch(0xE3FA, "1018", "Nro de marca enviado no coincidente al usado en el GetCard"),
    EvoCashRegisterNumberMismatch(0xE3FB, "1019", "Nro de caja enviado no coincidente al usado en el GetCard"),
    EvoAmountFieldMissing(0xE3FC, "1020", "Campo de monto no fue recibido"),
    EvoProxyCommunicationError(0xE3FE, "1022", "Error de comunicación con el proxy"),
    EvoTimeoutReached(0xE3FF, "1023", "Tiempo máximo de respuesta alcanzado"), 
    EvoTelClientInUse(0xE401, "1025", "TelClient en uso. Vuelva a intentarlo nuevamente"),
    EvoTransactionNotFound(0xE402, "1026", "Transacción no encontrada"),
    EvoTransactionAlreadyReversed(0xE403, "1027", "Transacción ya anulada, devuelta o reversada"),
    EvoInvalidRefundAmount(0xE404, "1028", "Importe a anular o devolver inválido"),
    EvoDatabaseError(0xE405, "1029", "Error de base de datos"),
    EvoMissingRequiredFields(0xE406, "1030", "Campos requeridos faltantes"),
    EvoDeviceAndModeRequired(0xE40A, "1034", "Tipo de dispositivo y modo de lectura son requeridos"),
    EvoDeviceTypeUnrecognized(0xE40B, "1035", "Tipo de dispositivo no reconocido"),
    EvoReadingModeUnrecognized(0xE40C, "1036", "Modo de lectura no reconocido"),
    EvoMissingFieldsForDevice(0xE40D, "1037", "Para el dispositivo y el modo de lectura enviados faltan campos requeridos"),
    EvoDifferentDeviceForRefund(0xE40E, "1038", "Dispositivo usado para la compra distinto al usado para la anulación/devolución"),
    EvoPendingReversal(0xE40F, "1039", "Transacción pendiente de reversa para esta terminal"),
    EvoTerminalNotFoundForMerchant(0xE410, "1040", "Nro de terminal no encontrado para el nro de comercio del plan elegido"),
    EvoTransactionCannotBeReversed(0xE411, "1041", "La transacción no puede ser devuelta o anulada"),
    EvoMissingAuthFields(0xE412, "1042", "Campos a ser autorizados por el punto de venta faltantes"),
    EvoCashRegisterNotConfigured(0xE413, "1043", "Caja no configurada"),
    EvoCardMismatch(0xE414, "1044", "Tarjeta leída no coincidente con la usada en la transacción a devolver o anular"),
    EvoInvalidCardReading(0xE415, "1045", "Lectura de tarjeta no válida para transaccionar"),
    EvoInvalidCompany(0xE417, "1047", "Compañía no válida"),
    EvoMissingCompanyConfiguration(0xE418, "1048", "Versión de configuración no encontrada para la compañía requerida"),
    EvoMissingVerificationFields(0xE419, "1049", "Campos a ser verificados por el punto de venta faltantes"),
    EvoLast4DigitsMismatch(0xE41A, "1050", "Últimos 4 dígitos no coincidentes"),
    EvoExpirationDateMismatch(0xE41B, "1051", "Fecha de vencimiento no coincidente"),
    EvoCardExpiredReading(0xE41E, "1054", "Lectura de tarjeta expirada. Debe releer la tarjeta"),
    EvoHostAddressNotFound(0xE41F, "1055", "Host y puerto del host no encontrado"),
    EvoTransactionNotAuthorizedByHost(0xE420, "1056", "La transacción a devolver/anular no fue autorizada por el host cuando fue realizada"),
    EvoTefClientCommunicationError(0xE421, "1057", "Error de comunicación con el Tef Client"),
    EvoTefClientTimeout(0xE422, "1058", "Timeout de conexión con el Tef Client"),
    EvoDecryptionError(0xE423, "1059", "Descifrado de datos leídos incorrecto. Reintente lectura"),
    EvoCashbackNotAllowed(0xE424, "1060", "Plan elegido no permite cashback"),
    EvoMinimumCashbackNotReached(0xE425, "1061", "Monto mínimo de compra para uso de cashback no superado"),
    EvoMinimumCashbackAmountMissing(0xE426, "1062", "Monto mínimo de cashback no alcanzado"),
    EvoMaximumCashbackExceeded(0xE427, "1063", "Monto máximo de cashback superado"),
    EvoDeferredPlanNotAllowed(0xE428, "1064", "Plan elegido no permite diferimiento"),
    EvoMinDeferredDaysNotReached(0xE429, "1065", "Mínimo de días de diferimiento no alcanzado"),
    EvoMaxDeferredDaysExceeded(0xE42A, "1066", "Máximo de días de diferimiento superado"),
    EvoMemcachedConnectionError(0xE42E, "1070", "No se ha podido conectar con el servicio de memcached"),
    EvoProxyStatusNotFound(0xE42F, "1071", "Estado del proxy no encontrado"),
    EvoMalformedHostConfiguration(0xE430, "1072", "Configuración de host mal formada"),
    EvoInvalidCardDataRead(0xE431, "1073", "Datos leídos de la tarjeta inválidos. Reintente lectura"),
    EvoMerchantNotConfigured(0xE435, "1077", "Número de comercio no configurado para el plan elegido"),
    EvoInvalidHostConfiguration(0xE436, "1078", "Configuración de host mal formada"),
    EvoTefServerUnavailable(0xE437, "1079", "Códigos de respuesta no disponibles. TefServer no operativo"),
    EvoAccountTypeNotFound(0xE439, "1081", "Tipo de cuenta no encontrado"),
    EvoProductTypeNotFound(0xE43A, "1082", "Tipo de producto no encontrado"),
    EvoCompanyNotConfigured(0xE43B, "1083", "Compañía no configurada"),
    EvoBankNotFound(0xE43C, "1084", "Banco indicado para la marca no encontrado"),
    EvoBranchNotConfigured(0xE43D, "1085", "Sucursal no configurada"),
    EvoNoTerminalSetted(0xE49C, "1086", "Terminal No configurada"), 
    EvoCardRequiredForRefund(0xE43F, "1087", "No es posible anular/devolver sin lectura previa de la tarjeta"),
    EvoCashRegisterRefundNotAllowed(0xE440, "1088", "Caja no habilitada para anular/devolver sin lectura previa de la tarjeta"),
    EvoCardLengthMismatch(0xE441, "1089", "Longitud de tarjeta no coincidente para la marca"),
    EvoTransactionIdMissing(0xE442, "1090", "Id de transacción a anular o devolver no recibido"),
    EvoMessageTypeMissing(0xE443, "1091", "Tipo de mensaje faltante"),
    EvoProcessingCodeMissing(0xE444, "1092", "Código de procesamiento faltante"),
    EvoTerminalNumberMissing(0xE445, "1093", "Número de terminal faltante"),
    EvoMerchantNumberMissing(0xE446, "1094", "Número de comercio faltante"),
    EvoInstallmentsCountMissing(0xE447, "1095", "Cantidad de cuotas faltante"),
    EvoPlanTypeMissing(0xE448, "1096", "Tipo de plan faltante"),
    EvoCurrencyTypeMissing(0xE449, "1097", "Tipo de moneda externo faltante"),
    EvoTraceNumberMissing(0xE44A, "1098", "Número de trace para esta terminal faltante"),
    EvoCardReadingModeMissing(0xE44B, "1099", "Modo de lectura de la tarjeta faltante"),
    EvoNiiMissing(0xE44C, "1100", "NII faltante"),
    EvoSoftwareVersionMissing(0xE44D, "1101", "Versión de software faltante"),
    EvoTicketNumberMissing(0xE44E, "1102", "Número de ticket faltante"),
    EvoEntryModeMissing(0xE44F, "1103", "Modo de ingreso faltante"),
    EvoPosCodeMissing(0xE450, "1104", "Código de POS faltante"),
    EvoTransactionTypeMissing(0xE451, "1105", "Tipo de transacción faltante"),
    EvoTransmissionTimeMissing(0xE452, "1106", "Tiempo y hora de transmisión faltante"),
    EvoTransactionDateMissing(0xE453, "1107", "Fecha y hora de la transacción faltante"),
    EvoInvalidTransmissionDateFormat(0xE455, "1109", "Fecha y hora de transmisión no está bien formateada"),
    EvoInvalidTransactionDateFormat(0xE456, "1110", "Fecha y hora de la transacción no está bien formateada"),
    EvoBatchNumberMissing(0xE458, "1112", "Número de lote faltante"),
    EvoClosingMerchantNumberMissing(0xE459, "1113", "Número de comercio de cierre faltante"),
    EvoResolutionModeMissing(0xE45A, "1114", "Modo de resolución faltante"),
    EvoTransactionAmountMissing(0xE45B, "1115", "Monto de la transacción faltante"),
    EvoHostIdMissing(0xE45C, "1116", "Id de host faltante"),
    EvoDeviceTypeMissing(0xE45D, "1117", "Tipo de dispositivo faltante"),
    EvoOriginalTicketNumberMissing(0xE45E, "1118", "Número de ticket de la transacción original faltante"),
    EvoOriginalTransactionDateMissing(0xE45F, "1119", "Fecha y hora de la transacción original faltante"),
    EvoEncryptedPacketMissing(0xE462, "1122", "Paquete cifrado faltante"),
    EvoExpirationDateMissing(0xE463, "1123", "Fecha de expiración faltante"),
    EvoCardNumberMissing(0xE464, "1124", "Número de tarjeta faltante"),
    EvoTrack1Missing(0xE465, "1125", "Track I faltante"),
    EvoTrack1FlagMissing(0xE466, "1126", "Flag de envío de track I faltante"),
    EvoWorkingKeySlotMissing(0xE467, "1127", "Slot de workingkey faltante"),
    EvoPaymentMethodIdMissing(0xE469, "1129", "ID de medio de pago faltante"),
    EvoCaptureDateMissing(0xE46A, "1130", "Fecha y hora de captura de la transacción faltante"),
    EvoInvalidCaptureDateFormat(0xE46B, "1131", "Fecha y hora de captura de la transacción no está bien formateada"),
    EvoInvalidDeviceUsed(0xE46C, "1132", "Tipo de dispositivo utilizado no reconocido"),
    EvoMaxCashbackExceeded(0xE46D, "1133", "Monto máximo de compra para uso de cashback superado"),
    EvoDeviceRejectedOperation(0xE46E, "1134", "Operación rechazada por el dispositivo"),
    EvoTransactionTypeMissingForRefund(0xE46F, "1135", "Tipo de transacción a anular y devolver no presente"),
    EvoOriginalRrnMissing(0xE470, "1136", "RRN de la transacción original no presente"),
    EvoOriginalTicketMissing(0xE471, "1137", "Ticket de la transacción original no presente"),
    EvoOriginalDateMissing(0xE472, "1138", "Hora y fecha de la transacción original no presente"),
    EvoOriginalTraceMissing(0xE473, "1139", "Número de trace de la transacción original no presente"),
    EvoOriginalAuthCodeMissing(0xE474, "1140", "Código de autorización de la transacción original no presente"),
    EvoAuthCodeRequiredOffline(0xE475, "1141", "Código de autorización requerido para transacciones offline"),
    EvoInvalidEncryptionType(0xE476, "1142", "Tipo de cifrado no válido"),
    EvoInvalidConfiguredDevice(0xE477, "1143", "Tipo de dispositivo configurado inválido"),
    EvoTransactionAmountRequired(0xE478, "1144", "Monto de la transacción requerido"),
    EvoTransactionTypeRequired(0xE479, "1145", "Tipo de transacción a realizar requerido"),
    EvoOriginalTransactionTypeRequired(0xE47A, "1146", "Tipo de transacción original a realizar requerido"),
    EvoDeviceIdNotFound(0xE47B, "1147", "ID del dispositivo no encontrado. Existe un error de configuración"),
    EvoDeviceIdNotConfigured(0xE47C, "1148", "ID del dispositivo no configurado"),
    EvoClosingProcessInProgress(0xE47D, "1149", "Proceso de cierre en curso para esta terminal"),
    EvoSyncTerminalIdMissing(0xE47E, "1150", "ID de terminal para sincronización faltante"),
    EvoChipReadExpired(0xE480, "1152", "Lectura chip EMV expirada. Relea la tarjeta y apresúrese"),
    EvoTechnicianNumberNotRecognized(0xE481, "1153", "Número de técnico no reconocido. Compruébelo y reintente"),
    EvoTaskNotEnabledForTechnician(0xE482, "1154", "Tarea no habilitada para este número de técnico"),
    EvoSerialNumberMissing(0xE484, "1156", "Número de serie lógico no presente. No podemos realizar la renovación de llaves"),
    EvoInvalidManualCardEntry(0xE485, "1157", "Número de tarjeta ingresado no válido"),
    EvoExpiredCard(0xE486, "1158", "Tarjeta vencida, o fecha de expiración ingresada incorrecta"),
    EvoDeviceTypeNotRecognized(0xE487, "1159", "Tipo de dispositivo no reconocido"),
    EvoSystemIdMissing(0xE488, "1160", "ID de sistema no presente"),
    EvoCompanyIdMissing(0xE489, "1161", "ID de empresa no presente"),
    EvoMerchantMismatchWithPlan(0xE48A, "1162", "El número de comercio recibido no es igual al establecido en el plan seleccionado"),
    EvoInstallmentsMismatchWithPlan(0xE48B, "1163", "La cantidad de cuotas recibida no es igual al establecido en el plan seleccionado"),
    EvoPlanTypeMismatchWithPlan(0xE48C, "1164", "El tipo de plan recibido no es igual al establecido en el plan seleccionado"),
    EvoCurrencyTypeMismatchWithPlan(0xE48D, "1165", "El tipo de moneda recibido no es igual al establecido en el plan seleccionado"),
    EvoOperationNotFoundInSystem(0xE48E, "1166", "Operación no encontrada"),
    EvoTranslationValueMissing(0xE48F, "1167", "El valor para la traducción del elemento no fue encontrado"),
    EvoCommunicationErrorGeneral(0xE490, "1168", "Comunication ERROR"), 
    EvoTransactionNotFoundInSystem(0xE491, "1169", "Transacción no encontrada"),
    EvoSystemIdentificationMissing(0xE49A, "1178", "System Identification inexistente"),
    EvoDoesntExistTerminals(0xE49B, "2069", "No existen terminales libres para utilizar, intente nuevamente")


    ;

    companion object{
        private val map = values().associateBy(
            ErrorCode::errorCodeInt)


        private val mapRes =values().associateBy(
            ErrorCode::responseCode)

        fun fromResponseCode(responseCode: String) :ErrorCode{
            if(responseCode == "68") return HostTimeOut
             if(responseCode == "H9") return ClientTimeOut
            return mapRes[responseCode] ?: return Undefined

        }
          fun fromId(errorCodeInt: Int) :ErrorCode{
            val errorCode=map[errorCodeInt]
            if(errorCode==null) return Undefined

            return errorCode
        }

    }





}
class WebServiceException(
    var errorMessage: String,
    var watcher: Watcher
) : Exception(errorMessage) {

    var errorCode: Int = 99
    var hostType = HostType.Undefined
    var terminalSerialNumber: String = ""
    var serverName: String = AppSettings.getInstance().serverName
    var rrn: String = ""

    companion object {
        private fun compose(errorCode: ErrorCode, extra: String?): String =
            buildString {
                append(errorCode.errorCodeMessage)
                if (!extra.isNullOrBlank()) append(' ').append(extra)
            }

        private fun compose(errorCode: ErrorCode, extra: String?, fd: Iso8583FieldDescriptor): String {
            val fieldMessage =
                "  FIELD (${fd.fieldNumber}): \"${fd.fieldName}\" VARIABLE: \"${fd.fieldVarName}\"  \n" +
                " PLEASE CHECK THE DOCUMENTATION\n ${fd.fieldDescription} "
            return buildString {
                append(errorCode.errorCodeMessage)
                if (!extra.isNullOrBlank()) append(' ').append(extra)
                append(' ').append(fieldMessage)
            }
        }

        private fun compose(errorCode: ErrorCode, extra: String?, tf: TokenFieldDescriptor): String {
            val emvTagMsg = if (tf.emvTag != "") ",(EMV_TAG -> \"${tf.emvTag}\")," else ""
            val fieldMessage =
                "  TOKEN \"${tf.tokenId}\" $emvTagMsg FIELD (${tf.fieldNumber}): \"${tf.fieldName}\" " +
                "VARIABLE \"${tf.fieldVarName}\" \n PLEASE CHECK THE DOCUMENTATION \n ${tf.fieldDescription} "
            return buildString {
                append(errorCode.errorCodeMessage)
                if (!extra.isNullOrBlank()) append(' ').append(extra)
                append(' ').append(fieldMessage)
            }
        }

        private fun compose(errorCode: ErrorCode, extra: String?, td: TransactionData): String {
            val fieldMessage =
                "  Merchant  \"${td.merchantType.merchantTypeName}\" When is   " +
                "\"${td.transactionType.transactionTypeName}\" and CardType: \"${td.cardType}\"  "
            return buildString {
                append(errorCode.errorCodeMessage)
                if (!extra.isNullOrBlank()) append(' ').append(extra)
                append(' ').append(fieldMessage)
            }
        }
    }

    constructor(errorCode: ErrorCode, watcher: Watcher)
        : this(errorCode.errorCodeMessage, watcher) {
        this.errorCode = errorCode.errorCodeInt
        hostType = watcher.getHostType()
        terminalSerialNumber = watcher.getTerminalSerialNumber()
        rrn = watcher.getRRN()
        watcher.error(errorCode, errorMessage)
    }

    constructor(
        errorCode: ErrorCode,
        message: String?,
        fieldDescriptor: Iso8583FieldDescriptor,
        watcher: Watcher
    ) : this(compose(errorCode, message, fieldDescriptor), watcher) {
        this.errorCode = errorCode.errorCodeInt
        hostType = watcher.getHostType()
        terminalSerialNumber = watcher.getTerminalSerialNumber()
        rrn = watcher.getRRN()
        watcher.error(errorCode, errorMessage)
    }

    constructor(
        errorCode: ErrorCode,
        message: String?,
        fieldDescriptor: TokenFieldDescriptor,
        watcher: Watcher
    ) : this(compose(errorCode, message, fieldDescriptor), watcher) {
        this.errorCode = errorCode.errorCodeInt
        hostType = watcher.getHostType()
        terminalSerialNumber = watcher.getTerminalSerialNumber()
        rrn = watcher.getRRN()
        watcher.error(errorCode, errorMessage)
    }

    constructor(
        errorCode: ErrorCode,
        message: String?,
        transactionData: TransactionData,
        watcher: Watcher
    ) : this(compose(errorCode, message, transactionData), watcher) {
        this.errorCode = errorCode.errorCodeInt
        hostType = watcher.getHostType()
        terminalSerialNumber = watcher.getTerminalSerialNumber()
        rrn = watcher.getRRN()
        watcher.error(errorCode, errorMessage)
    }

    constructor(ex: Exception, watcher: Watcher)
        : this(ErrorCode.UnHandledException, ex.message, watcher)

    constructor(errorCode: ErrorCode, message: String?, watcher: Watcher)
        : this(compose(errorCode, message), watcher) {
        this.errorCode = errorCode.errorCodeInt
        hostType = watcher.getHostType()
        terminalSerialNumber = watcher.getTerminalSerialNumber()
        rrn = watcher.getRRN()
        watcher.error(errorCode, errorMessage)
    }
}

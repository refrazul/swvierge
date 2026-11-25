package com.viergegroup.swmain.websecurity

object SecurityConstants {
    var poolId: String="us-east-1_0zxZspCeh"

    var clientId: String="3sa13618lfn4os8ad5bqbnei20"
    var clientSecret: String="6a494gv7im93p9ad44dq4idjc0cbbh87pmjng4q9m9fsrfcd569"
    var introspectionUri: String="https://switchvg.viergegroup.com/users/test"
    var tokenUri = "https://viergegroup.auth.us-east-1.amazoncognito.com/oauth2/token"
    var projectId = "backendvierge"
    var uroCognito = "https://cognito-idp.us-east-1.amazonaws.com/{$poolId}"

    var wellUri="https://accounts.google.com/.well-known/openid-configuration"
    var scopes=listOf("email","profile","openid")
}
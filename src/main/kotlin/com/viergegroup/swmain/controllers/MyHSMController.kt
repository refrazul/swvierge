package com.viergegroup.swmain.controllers

import com.viergegroup.crypto.security.MyHsmDto
import com.viergegroup.crypto.security.MyHsmManager
import com.viergegroup.swmain.managers.PandoraneoManager
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("myhsm")
class MyHSMController {

  //  val myHsmManager= MyHsmManager()

    @PostMapping("/encrypt-dukpt") //  
    fun encryptDukpt(@RequestBody  request: MyHsmDto. DUKPTRequest): MyHsmDto. DUKPTResponse {
        return PandoraneoManager().encryptWithDUKPT(request)
    }
    @PostMapping("/decrypt-dukpt") // 
    fun decryptDukpt(@RequestBody  request: MyHsmDto. DUKPTRequest): MyHsmDto. DUKPTResponse {
        return PandoraneoManager().decryptWithDUKPT(request)
    }
    @PostMapping("/encrypt-pin") // 
    fun encryptPIN(@RequestBody  request: MyHsmDto. PINHSMRequest): MyHsmDto. PINHSMResponse {
        return PandoraneoManager().encryptPIN(request)
    }

    @PostMapping("/decrypt-pin") // 
    fun decryptPIN(@RequestBody  request: MyHsmDto. PINHSMRequest): MyHsmDto. PINHSMResponse {
        return PandoraneoManager().decryptPIN(request)
    }
 
    @PostMapping("/trans-pin") // 
    fun translatePIN(@RequestBody  request: MyHsmDto. PINHSMRequest): MyHsmDto. PINHSMResponse {
        return PandoraneoManager().translatePIN(request)
    }







    @GetMapping("/test") //
    fun test():String{
        return "ok"
    }

}
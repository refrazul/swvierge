package com.viergegroup.swmain.entities.catalogs

enum class LogLevel(val code:String) {
    FileDebug("fd"),
    FileInfo("fi"),
    FileWarning("fw"),
    FileError("fe"),
     ConsoleDebug("cd"),
    ConsoleInfo("ci"),
    ConsoleWarning("cw"),
    ConsoleError("ce"),
     ConsoleCommRaw("cmr"),
     ConsoleCommError("cme")

}
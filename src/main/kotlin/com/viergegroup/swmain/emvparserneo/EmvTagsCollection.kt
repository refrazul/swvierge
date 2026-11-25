package com.viergegroup.swmain.emvparserneo

class EmvTagsCollection {
    var emvTags= mutableListOf<EmvTagData>()
    var primitiveEmvTags= mutableListOf<EmvTagData>()

    // var emvConstructedTags= mutableListOf<EmvConstructedTagData>()
    var errorMessage=""
}
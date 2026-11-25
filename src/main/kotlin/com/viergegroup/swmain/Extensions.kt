package com.viergegroup.swmain

import java.util.*

object Extensions {
    fun <T> getListFromIterator(iterator: Iterator<T>): List<T> {

        // Create an empty list
        val list: MutableList<T> = ArrayList()
 
        // Add each element of iterator to the List
        iterator.forEachRemaining { e: T -> list.add(e) }

        // Return the List
        return list
    }
}
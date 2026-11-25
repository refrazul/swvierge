package com.viergegroup.crypto.security

class BitSet : java.util.BitSet{

    companion object{
        val DEFAULT_SIZE = 8
        private val serialVersionUID = 1L
    }

    private var size = DEFAULT_SIZE


    /**
     * Creates a BitSet with DEFAULT_SIZE bits.
     */
    constructor():super(DEFAULT_SIZE) {

        size = DEFAULT_SIZE
    }

    /**
     * Creates a BitSet with a specified number of bits.
     * @param nbits The size of the created BitSet.
     */
    constructor(nbits: Int):super(nbits) {

        size = nbits
    }

    override operator fun get(low: Int, high: Int): BitSet {
        val n = BitSet(high - low)
        for (i in 0 until high - low) {
            n.set(i, this.get(low + i))
        }
        return n
    }

    /**
     * Returns the size of the BitSet as declared or requested (the fixed-length).
     */
    fun bitSize(): Int {
        return size
    }
}
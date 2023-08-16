package cat.kiwi.clrwalk

fun findBytesInByteArray(byteArray: ByteArray, bytes: ByteArray): List<Int> {
    val result = mutableListOf<Int>()
    for (i in byteArray.indices) {
        var flag = true
        for (j in bytes.indices) {
            if (byteArray[i + j] != bytes[j]) {
                flag = false
                break
            }
        }
        if (flag) {
            result.add(i)
        }
    }
    return result
}

val Number.printableHexString: String
    get() = "%08X".format(this)
val UInt.printableHexString: String
    get() = "%08X".format(this.toInt())
val ULong.printableHexString: String
    get() = "%08X".format(this.toInt())
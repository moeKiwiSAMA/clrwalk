package cat.kiwi.clrwalk.model

data class DoNetPEDll(
    val memDump: ByteArray,
    val dosHeaderOffset: Int,
    val csharpMetadataHeaderOffset: Int,
    val peName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DoNetPEDll

        if (!memDump.contentEquals(other.memDump)) return false
        if (dosHeaderOffset != other.dosHeaderOffset) return false
        if (csharpMetadataHeaderOffset != other.csharpMetadataHeaderOffset) return false
        return peName == other.peName
    }

    override fun hashCode(): Int {
        var result = memDump.contentHashCode()
        result = 31 * result + dosHeaderOffset.hashCode()
        result = 31 * result + csharpMetadataHeaderOffset.hashCode()
        result = 31 * result + peName.hashCode()
        return result
    }
}

val DoNetPEDll.getPeHeaderOffset: Int
    get() {
        val peHeaderOffsetIndex = this.dosHeaderOffset + 0x3C
        val peHeaderOffsetByte =
            memDump.copyOfRange(peHeaderOffsetIndex.toInt(), peHeaderOffsetIndex.toInt() + 2).map { it.toUByte() }
        return (peHeaderOffsetByte[1] * 256u + peHeaderOffsetByte[0]).toInt() + dosHeaderOffset
    }
val DoNetPEDll.peImageSizeIndexOffset: Int
    get() {
        return this.getPeHeaderOffset + 0x50
    }
val DoNetPEDll.getPeImageSize: Int
    get() {
        val peImageSizeIndex = this.peImageSizeIndexOffset
        val peImageSizeByte =
            memDump.copyOfRange(peImageSizeIndex, peImageSizeIndex + 4)
        return (peImageSizeByte[3] * 256 * 256 * 256 + peImageSizeByte[2] * 256 * 256 + peImageSizeByte[1] * 256 + peImageSizeByte[0]).toInt()
    }

val DoNetPEDll.peImageEnd: Int
    get() {
        return this.getPeHeaderOffset + this.getPeImageSize
    }

val DoNetPEDll.fileSize: String
    get() {
        val size = getPeImageSize.toDouble()
        if (size >= 1024 * 1024) {
            // keep 2 decimal places
            return String.format("%.2fMiB", size / 1024 / 1024)
        }
        if (size >= 1024) {
            return String.format("%.2fKiB", size / 1024)
        }
        return "${size}B"
    }

package cat.kiwi.clrwalk

import cat.kiwi.clrwalk.model.*
import java.io.File
import java.util.UUID


class CLRWalk {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                println("usage: java -jar clrwalk.jar <path>")
                return
            }
            val memDumpPath = args[0]
            val file = File(memDumpPath)
            if (!file.exists()) {
                println("file not found")
                return
            }
            // holy shit, just read it into memory, no KMP, no block reading, 2GiB - 1B size limit
            val fileByteArray = file.readBytes()

            // get all pe headers
            val peHeadersIndex = findBytesInByteArray(fileByteArray, peHeaderIdentifier)

            // get csharp metadata headers
            val csharpMetadataHeaderOffsets = findBytesInByteArray(fileByteArray, csharpMetadataIdentifier)
            val doNetDllList = arrayListOf<DoNetPEDll>()
            for (i in csharpMetadataHeaderOffsets.indices) {
                try {
                    val csharpMetadataHeaderOffset = csharpMetadataHeaderOffsets[i]
                    val peHeaderOffset = peHeadersIndex.filter { (csharpMetadataHeaderOffset - it) > 0 }.max()
                    val peNameBuffer = fileByteArray.copyOfRange(
                        csharpMetadataHeaderOffset + 34,
                        csharpMetadataHeaderOffset + 200
                    )
                    var peName = ""
                    try {
                        peName = peNameBuffer.toString(Charsets.UTF_16LE).split("\u0000")[0]
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    doNetDllList.add(
                        DoNetPEDll(
                            fileByteArray,
                            peHeaderOffset,
                            csharpMetadataHeaderOffset,
                            peName
                        )
                    )
                } catch (_: Exception) {
                }
            }
            doNetDllList.sortBy { it.peName }

            println("Summary")
            println("===== PE Headers =====")
            println("counts: ${peHeadersIndex.size}")
            println("offsets list:")
            peHeadersIndex.windowed(10, 10).forEach {
                it.forEach { index ->
                    print("%08X".format(index))
                    print(" ")
                }
                println()
            }
            println()
            println("===== DoNet CSharp Binaries =====")
            println("counts: ${doNetDllList.size}")
            println("----------")
            for (dll in doNetDllList) {
                println("PE Name: ${dll.peName}")
                println("DOS Header Offset: ${dll.dosHeaderOffset.printableHexString}")
                println("PE Header Offset: ${dll.getPeHeaderOffset.printableHexString}")
                println("PE Image End Offset: ${dll.peImageEnd.printableHexString}")
                println("CSharp Metadata Header Offset: ${dll.csharpMetadataHeaderOffset.printableHexString}")
                println("PE Image Size Index Offset: ${dll.peImageSizeIndexOffset.printableHexString}")
                println("PE Image Size: ${dll.fileSize}")
                println("----------")
            }
            println("Dumping DoNet CSharp Binaries...")
            // if dir output not exists, create it
            val outputDir = File("output")
            if (!outputDir.exists()) {
                outputDir.mkdir()
            }
            val errorDump = arrayListOf<String>()
            var succeedCount = 0
            for (dll in doNetDllList) {
                try {
                    // keep eng digit . and _ only
                    var cleanName = dll.peName.filter { it.isLetterOrDigit() || it == '.' || it == '_' || it == '-' }
                    if (cleanName.isEmpty()) {
                        cleanName = "unknownName-${UUID.randomUUID()}"
                    }
                    val dllFile = File("output/${cleanName}")
                    println("Dumping ${dll.peName} to ${dllFile.absolutePath}")
                    println("DOS Header Offset: ${dll.dosHeaderOffset.printableHexString}")
                    println("PE Image End: ${dll.peImageEnd.printableHexString}")
                    if (dll.getPeImageSize > 1024 * 1024 * 50) {
                        println("PE Image Size is larger than 50 MiB, skip")
                    }
                    dllFile.writeBytes(
                        dll.memDump.copyOfRange(
                            dll.dosHeaderOffset,
                            (dll.getPeHeaderOffset + dll.getPeImageSize + 1024 * 50)
                        )
                    )
                    succeedCount++
                    println("Dumping succeed: ${dll.peName}")
                } catch (e: Exception) {
                    errorDump.add(dll.peName)
                    println("Dumping error: ${e.message}, ${dll.peName}")
                }
                println("----------")
            }

            println("Dumping finished, succeed: ${succeedCount}, error: ${errorDump.size}")
            if (errorDump.size > 0) {
                println("Error list:")
                errorDump.forEach {
                    println(it)
                }
            }
        }
    }
}


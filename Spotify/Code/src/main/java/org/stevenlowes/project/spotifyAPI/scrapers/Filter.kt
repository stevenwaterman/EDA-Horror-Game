package org.stevenlowes.project.spotifyAPI.scrapers

import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter

class Filter {
    companion object {
        fun apply() {
            println("Applying filter")

            BufferedReader(FileReader("analysedData.txt")).useLines { seq ->
                FileWriter("filteredData.txt").use { writer ->
                    seq.filter { line ->
                        val split = line.split(" ")

                        if (split.size != 27) { //Size
                            return@filter false
                        }

                        if (split[1] == "error") { //Errors
                            return@filter false
                        }

                        val values = split.asSequence().drop(1).mapNotNull { it.toFloatOrNull() }.toList()
                        if (values.size != 26){
                            return@filter false
                        }

                        if (values[5] > 0.6) { //Speechiness
                            return@filter false
                        }

                        if (values[8] > 0.85) { //Liveness
                            return@filter false
                        }

                        if (values[3] < -55) { // Loudness
                            return@filter false
                        }

                        if (values[11] >= 5 * 60 * 1000) { //Duration
                            return@filter false
                        }

                        if (values[11] <= 2 * 60 * 1000) { //Duration
                            return@filter false
                        }

                        if (values.count { it == 0f } > 4) { //Probably not music
                            return@filter false
                        }

                        return@filter true
                    }.forEach { writer.write("$it\n") }
                }
            }

            println("Done applying filter")
        }
    }
}

fun main(args: Array<String>) {
    Filter.apply()
}
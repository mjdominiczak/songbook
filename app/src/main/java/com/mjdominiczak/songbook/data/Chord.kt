package com.mjdominiczak.songbook.data

data class Chord(
    val base: ChordBase,
    val suffix: String = "",
    var transposition: Int = 0,
) : Comparable<Chord> {
    val length: Int
        get() = this.toString().length

    companion object {
        fun fromString(string: String): Chord? {
            ChordBase.values().forEach {
                if (string.startsWith(prefix = it.name)) {
                    return Chord(
                        base = it,
                        suffix = string.removePrefix(it.name)
                    )
                }
            }
            return null
        }
    }

    override fun compareTo(other: Chord): Int {
        return if (this.base == other.base) this.suffix.compareTo(other.suffix)
        else this.base.index.compareTo(other.base.index)
    }

    override fun toString(): String {
        return "$base$suffix"
    }

    fun transpose(semitones: Int) =
        this.copy(
            base = ChordBase.valueOf(
                chordsList[(chordsList.indexOf(this.base.name) + 2 * semitones).mod(chordsList.size)]
            )
        )
}

val chordsList = listOf(
    "C",
    "c",
    "Cis",
    "cis",
    "D",
    "d",
    "Es",
    "es",
    "E",
    "e",
    "F",
    "f",
    "Fis",
    "fis",
    "G",
    "g",
    "As",
    "as",
    "A",
    "a",
    "B",
    "b",
    "H",
    "h",
)

enum class ChordBase(val index: Int) {
    Cis(2), cis(3),
    Es(6), es(7),
    Fis(12), fis(13),
    As(16), `as`(17),
    C(0), c(1),
    D(4), d(5),
    E(8), e(9),
    F(10), f(11),
    G(14), g(15),
    A(18), a(19),
    B(20), b(21),
    H(22), h(23),
}
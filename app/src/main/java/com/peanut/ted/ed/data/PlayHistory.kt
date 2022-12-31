package com.peanut.ted.ed.data

data class PlayHistory(val id: String, val url: String){

    companion object{
        val Empty = PlayHistory("", "")
        fun fromEpisode(e: Episode, album: String, title: String) = PlayHistory(
            e.episodeName, e.getRawLink(album, title)
        )
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayHistory

        if (id != other.id) return false

        return true
    }
}


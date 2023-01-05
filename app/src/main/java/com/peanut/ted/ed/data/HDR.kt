package com.peanut.ted.ed.data

enum class HDR(val desc: String) {
    INVALID_LUMINANCE("不支持"),
    HDR_TYPE_DOLBY_VISION("Dolby Vision"),
    HDR_TYPE_HDR10("HDR10"),
    HDR_TYPE_HLG("Hybrid Log-Gamma(HLG)"),
    HDR_TYPE_HDR10_PLUS("HDR10+");

    companion object{
        fun fromType(type: Int): HDR {
            return when (type) {
                1 -> HDR_TYPE_DOLBY_VISION
                2 -> HDR_TYPE_HDR10
                3 -> HDR_TYPE_HLG
                4 -> HDR_TYPE_HDR10_PLUS
                else -> INVALID_LUMINANCE
            }
        }
    }
}
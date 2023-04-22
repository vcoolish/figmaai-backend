package com.app.figmaai.backend.credentials

interface EncoderDecoderService {
    fun encode(content: String): String
    fun decode(content: String): String
}

package com.godfather59.quransunnah.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader

// Android implementation of MushafMetadataReader using XmlPullParser.
// Parses quran-data.xml from bundled assets.

class AndroidMushafMetadataReader(private val context: Context) : MushafMetadataReader {

    override suspend fun load(): MushafMetadata = withContext(Dispatchers.IO) {
        val inputStream = context.assets.open("quran/metadata/quran-data.xml")
        val reader = InputStreamReader(inputStream, "UTF-8")
        try {
            parseXml(reader)
        } finally {
            reader.close()
        }
    }

    private fun parseXml(reader: InputStreamReader): MushafMetadata {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(reader)

        val juzStarts = mutableListOf<AyahRef>()
        val quarterStarts = mutableListOf<AyahRef>()
        val pageStarts = mutableListOf<AyahRef>()
        val sajdas = mutableListOf<AyahRef>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "juz" -> juzStarts.add(readAyahRef(parser))
                        "quarter" -> quarterStarts.add(readAyahRef(parser))
                        "page" -> pageStarts.add(readAyahRef(parser))
                        "sajda" -> sajdas.add(readAyahRef(parser))
                    }
                }
            }
            eventType = parser.next()
        }

        require(juzStarts.size == 30) { "Expected 30 juz starts, got ${juzStarts.size}" }
        require(quarterStarts.size == 240) { "Expected 240 quarter starts, got ${quarterStarts.size}" }
        require(pageStarts.size == 604) { "Expected 604 page starts, got ${pageStarts.size}" }
        require(sajdas.size == 15) { "Expected 15 sajdas, got ${sajdas.size}" }

        return MushafMetadata(
            juzStarts = juzStarts,
            quarterStarts = quarterStarts,
            pageStarts = pageStarts,
            sajdas = sajdas,
        )
    }

    private fun readAyahRef(parser: XmlPullParser): AyahRef {
        val surahAttr = parser.getAttributeValue(null, "sura") ?: parser.getAttributeValue(null, "index") ?: "1"
        val ayahAttr = parser.getAttributeValue(null, "aya") ?: "1"
        return AyahRef(surahAttr.toInt(), ayahAttr.toInt())
    }
}
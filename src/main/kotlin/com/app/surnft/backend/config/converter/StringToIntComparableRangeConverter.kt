package com.app.surnft.backend.config.converter

import com.app.surnft.backend.config.probability.ComparableRange
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils

@Component
@ConfigurationPropertiesBinding
class StringToIntComparableRangeConverter : Converter<String, ComparableRange<Int>> {

  override fun convert(source: String): ComparableRange<Int>? {
    if (ObjectUtils.isEmpty(source)) {
      return null
    }

    val strings = source.split('-', limit = 2)
    val start = strings.first()
    val end = strings.last()

    return ComparableRange(Integer.parseInt(end), Integer.parseInt(start))
  }
}
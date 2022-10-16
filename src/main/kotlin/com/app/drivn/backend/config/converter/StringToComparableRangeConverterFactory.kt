package com.app.drivn.backend.config.converter

import com.app.drivn.backend.config.probability.ComparableRange
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalConverter
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.springframework.util.ObjectUtils

class StringToComparableRangeConverterFactory : ConverterFactory<String, ComparableRange<*>>,
  ConditionalConverter {

  override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
    return true
  }

  override fun <T : ComparableRange<*>> getConverter(targetType: Class<T>): Converter<String, T> {
    return Converter {
      this.convert(it, targetType)
    }
  }

  private fun <T : ComparableRange<*>> convert(source: String, targetType: Class<T>): T? {
    if (ObjectUtils.isEmpty(source)) {
      return null
    }

    TypeDescriptor.valueOf(targetType)
    val strings = source.split('-', limit = 2)
    val start = strings.first()
    val end = strings.last()
    TODO("Not yet implemented")
  }
}

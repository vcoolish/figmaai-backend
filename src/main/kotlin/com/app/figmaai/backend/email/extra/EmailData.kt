package com.app.figmaai.backend.email.extra

class EmailData(
  val userPersonalData: UserEmailPersonalData,
  var emailType: EmailType,
  var dynamicData: Map<String, Any> = emptyMap(),
)
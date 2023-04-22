package com.app.figmaai.backend.config

import org.springframework.context.ApplicationEvent

class OverloadBeansEvent(vararg beanNames: String) : ApplicationEvent(beanNames)

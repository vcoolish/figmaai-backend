package com.app.figmaai.backend.config

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

class CustomOverloadingSingletonEventScopeRegistryBeanFactoryPostProcessor(
    private val overloadingScopeConfigurer: OverloadingSingletonEventRefreshableScopeConfigurer,
    private val switchScopeConfigurer: SwitchSingletonEventRefreshableScopeConfigurer
) : BeanFactoryPostProcessor {

    companion object {
        const val overloadingScope: String = "overloadingSingleton"
        const val switchScope: String = "switchSingleton"
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactory.registerScope(overloadingScope, overloadingScopeConfigurer)
        beanFactory.registerScope(switchScope, switchScopeConfigurer)
    }
}

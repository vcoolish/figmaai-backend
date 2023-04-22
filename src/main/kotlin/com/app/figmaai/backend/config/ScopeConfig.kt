package com.app.figmaai.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeConfig {

    @Bean
    fun overloadingSingletonEventRefreshableScopeConfigurer(): OverloadingSingletonEventRefreshableScopeConfigurer =
        OverloadingSingletonEventRefreshableScopeConfigurer()

    @Bean
    fun switchSingletonEventRefreshableScopeConfigurer(): SwitchSingletonEventRefreshableScopeConfigurer =
        SwitchSingletonEventRefreshableScopeConfigurer()

    @Bean
    fun customOverloadingSingletonEventScopeRegistryBeanFactoryPostProcessor(
        overloadingScopeConfigurer: OverloadingSingletonEventRefreshableScopeConfigurer,
        switchScopeConfigurer: SwitchSingletonEventRefreshableScopeConfigurer
    ): CustomOverloadingSingletonEventScopeRegistryBeanFactoryPostProcessor =
        CustomOverloadingSingletonEventScopeRegistryBeanFactoryPostProcessor(
            overloadingScopeConfigurer,
            switchScopeConfigurer
        )

    @Bean
    fun overloadBeansEventListener(scopeConfigurer: OverloadingSingletonEventRefreshableScopeConfigurer): OverloadBeansEventListener =
        OverloadBeansEventListener(scopeConfigurer)
}

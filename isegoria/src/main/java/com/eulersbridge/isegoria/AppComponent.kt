package com.eulersbridge.isegoria

import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [(AndroidSupportInjectionModule::class), (AppModule::class), (BuildersModule::class)]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: IsegoriaApp): Builder

        fun build(): AppComponent
    }

    fun inject(app: IsegoriaApp)
}
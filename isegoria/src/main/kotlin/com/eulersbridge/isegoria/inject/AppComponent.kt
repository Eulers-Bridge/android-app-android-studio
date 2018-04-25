package com.eulersbridge.isegoria.inject

import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.notifications.ServicesModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [(AndroidSupportInjectionModule::class), (AppModule::class),
        (ServicesModule::class), (ActivityBuilder::class)]
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
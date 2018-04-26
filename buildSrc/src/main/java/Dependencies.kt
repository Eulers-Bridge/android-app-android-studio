@file:Suppress("unused")

object Versions {
    const val kotlin = "1.2.40"
    const val android_gradle_plugin = "3.2.0-alpha12"
    const val google_gradle_plugin = "3.1.1"

    const val min_sdk = 16
    const val target_sdk = 27
    const val compile_sdk = 27
    const val version_code = 15
    const val version_name = "1.0.14"

    const val ktx = "0.3"
    const val multidex = "1.0.3"

    const val support_lib = "27.1.1"
    const val constraint_layout = "1.1.0"

    const val rxjava = "2.1.12"
    const val rxkotlin = "2.2.0"
    const val rxjava_reactive_streams = "1.2.1"

    const val arch_components = "1.1.1"
    const val dagger = "2.15"

    const val okhttp = "3.10.0"
    const val retrofit = "2.4.0"

    const val aws = "2.6.18"
    const val firebase = "15.0.0"
    const val secure_prefs = "v0.1.6"

    const val glide = "4.6.1"
    const val image_view = "3.10.0"
    const val image_cropper = "2.7.0"

    const val bottom_navigation = "1.2.4"
    const val view_pager_indicator = "2.4.1"

    const val junit = "4.12"
    const val mockito = "2.18.3"
    const val mockito_kotlin = "2.0.0-alpha03"
    const val espresso = "3.0.1"
}

object TestDeps {
    const val junit = "junit:junit:${Versions.junit}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockito_kotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockito_kotlin}"
    const val livedata_test = "android.arch.core:core-testing:${Versions.arch_components}"

    const val espresso = "com.android.support.test.espresso:espresso-core:${Versions.espresso}"
    const val espresso_intents = "com.android.support.test.espresso:espresso-intents:${Versions.espresso}"
}

object Deps {
    const val kotlin_gradle_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val android_gradle_plugin = "com.android.tools.build:gradle:${Versions.android_gradle_plugin}"
    const val google_gradle_plugin = "com.google.gms:google-services:${Versions.google_gradle_plugin}"

    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    const val multidex = "com.android.support:multidex:${Versions.multidex}"

    const val support_core_ui = "com.android.support:support-core-ui:${Versions.support_lib}"
    const val support_fragment = "com.android.support:support-fragment:${Versions.support_lib}"
    const val appcompat_v7 = "com.android.support:appcompat-v7:${Versions.support_lib}"
    const val design = "com.android.support:design:${Versions.support_lib}"
    const val recyclerview_v7 = "com.android.support:recyclerview-v7:${Versions.support_lib}"
    const val support_vector_drawable = "com.android.support:support-vector-drawable:${Versions.support_lib}"
    const val constraint_layout = "com.android.support.constraint:constraint-layout:${Versions.constraint_layout}"

    const val ktx = "androidx.core:core-ktx:${Versions.ktx}"

    const val rxjava2 =  "io.reactivex.rxjava2:rxjava:${Versions.rxjava}"
    const val rxkotlin = "io.reactivex.rxjava2:rxkotlin:${Versions.rxkotlin}"
    const val rxjava_reactive_streams = "io.reactivex:rxjava-reactive-streams:${Versions.rxjava_reactive_streams}"

    const val lifecycle_extensions = "android.arch.lifecycle:extensions:${Versions.arch_components}"
    const val lifecycle_java8 = "android.arch.lifecycle:common-java8:${Versions.arch_components}"
    const val lifecycle_reactive_streams = "android.arch.lifecycle:reactivestreams:${Versions.arch_components}"

    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val dagger_processor = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    const val dagger_android = "com.google.dagger:dagger-android:${Versions.dagger}"
    const val dagger_android_support = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    const val dagger_android_processor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"

    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okhttp_interceptor = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val retrofit_rxjava = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val retrofit_moshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val aws_s3 = "com.amazonaws:aws-android-sdk-s3:${Versions.aws}"
    const val aws_cognito = "com.amazonaws:aws-android-sdk-cognito:${Versions.aws}"
    const val aws_sns = "com.amazonaws:aws-android-sdk-sns:${Versions.aws}"

    const val firebase_messaging = "com.google.firebase:firebase-messaging:${Versions.firebase}"

    const val secure_prefs = "com.github.scottyab:secure-preferences:${Versions.secure_prefs}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val glide_processor = "com.github.bumptech.glide:compiler:${Versions.glide}"
    const val image_view = "com.davemorrissey.labs:subsampling-scale-image-view:${Versions.image_view}"
    const val image_cropper = "com.theartofdev.edmodo:android-image-cropper:${Versions.image_cropper}"

    const val bottom_navigation = "com.github.ittianyu:BottomNavigationViewEx:${Versions.bottom_navigation}"
    const val view_pager_indicator = "com.github.JakeWharton:ViewPagerIndicator:${Versions.view_pager_indicator}"
}
package com.eulersbridge.isegoria.network

import com.eulersbridge.isegoria.network.api.response.GenericPaginatedResponse
import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

/**
 * "Unwraps" API methods annotated with @Paginated by returning the contents of their `foundObjects`
 * rather than the entire response.
 */
internal class UnwrapConverterFactory : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<ResponseBody, *>? {

        return if (annotations.any { it.annotationClass == Paginated::class }) {

            val envelopeType = Types.newParameterizedType(GenericPaginatedResponse::class.java, type)

            val delegate = retrofit.nextResponseBodyConverter<GenericPaginatedResponse<*>>(this, envelopeType, annotations)

            Converter<ResponseBody, Any> { value ->
                val (foundObjects, totalElements) = delegate.convert(value)

                if (totalElements > 0) {
                    foundObjects
                } else {
                    null
                }
            }

        } else {
            null
        }
    }

}

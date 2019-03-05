package com.eulersbridge.isegoria.network

import com.eulersbridge.isegoria.network.api.Paginated
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
                val (foundObjects) = delegate.convert(value)

                foundObjects
                // NOTE: there was previously a check to return null if found objects is an empty
                //       list, this was removed because it was causing unnecessary errors and had no
                //        obvious purpose
            }

        } else {
            null
        }
    }

}

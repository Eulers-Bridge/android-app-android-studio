package com.eulersbridge.isegoria.network;

import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Seb on 04/11/2017.
 */

/**
 * "Unwraps" API methods annotated with @Paginated by returning the contents of their `foundObjects`
 * rather than the entire response.
 */
public class UnwrapConverterFactory extends Converter.Factory {

    private final GsonConverterFactory factory;

    public UnwrapConverterFactory(GsonConverterFactory factory) {
        this.factory = factory;
    }

    @Override
    public @Nullable
    Converter<ResponseBody, ?> responseBodyConverter(Type type,
                                                     Annotation[] annotations, Retrofit retrofit) {

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Paginated.class) {

                Type wrappedType = new ParameterizedType() {
                    @Override
                    public Type[] getActualTypeArguments() {
                        return new Type[] {type};
                    }

                    @Override
                    public Type getOwnerType() {
                        return null;
                    }

                    @Override
                    public Type getRawType() {
                        return GenericPaginatedResponse.class;
                    }
                };

                Converter<ResponseBody, ?> gsonConverter = factory
                        .responseBodyConverter(wrappedType, annotations, retrofit);

                //noinspection unchecked,unchecked
                return new ResponseWrapperBodyConverter(gsonConverter);
            }
        }

        return null;
    }

}

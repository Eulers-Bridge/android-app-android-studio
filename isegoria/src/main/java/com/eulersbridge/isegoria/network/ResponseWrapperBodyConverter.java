package com.eulersbridge.isegoria.network;

import java.io.IOException;

import okhttp3.ResponseBody;
        import retrofit2.Converter;

/**
 * Created by Seb on 04/11/2017.
 */

class ResponseWrapperBodyConverter<T>
        implements Converter<ResponseBody, T> {
    private final Converter<ResponseBody, GenericPaginatedResponse<T>> converter;

    ResponseWrapperBodyConverter(Converter<ResponseBody,
            GenericPaginatedResponse<T>> converter) {
        this.converter = converter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        GenericPaginatedResponse<T> response = converter.convert(value);

        return response.foundObjects;

    }
}
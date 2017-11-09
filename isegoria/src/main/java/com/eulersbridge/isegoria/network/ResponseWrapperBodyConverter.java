package com.eulersbridge.isegoria.network;

import java.io.IOException;

import okhttp3.ResponseBody;
        import retrofit2.Converter;

class ResponseWrapperBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Converter<ResponseBody, GenericPaginatedResponse<T>> converter;

    ResponseWrapperBodyConverter(Converter<ResponseBody,
            GenericPaginatedResponse<T>> converter) {
        this.converter = converter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        GenericPaginatedResponse<T> response = converter.convert(value);

        if (response.totalElements > 0) {
            return response.foundObjects;
        } else {
            return null;
        }
    }
}
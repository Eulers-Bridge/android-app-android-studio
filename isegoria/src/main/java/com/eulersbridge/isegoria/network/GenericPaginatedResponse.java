package com.eulersbridge.isegoria.network;

/**
 * Created by Seb on 05/11/2017.
 */

class GenericPaginatedResponse<T> {

    T foundObjects;
    public long totalElements;
    public long totalPages;

}

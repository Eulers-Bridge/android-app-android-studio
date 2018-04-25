package com.eulersbridge.isegoria.network

data class NetworkConfig(
        var baseUrl: String = DEFAULT_BASE_URL,
        val s3PicturesBucketName: String = "isegoriauserpics",
        val s3PicturesPath: String = "https://s3.amazonaws.com/isegoriauserpics/"
) {
    companion object {
        private const val DEFAULT_BASE_URL = "http://54.79.70.241:8080/dbInterface/api/"
    }

    fun resetBaseUrl() {
        this.baseUrl = DEFAULT_BASE_URL
    }
}
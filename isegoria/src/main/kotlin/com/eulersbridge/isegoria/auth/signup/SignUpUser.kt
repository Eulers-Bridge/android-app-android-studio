package com.eulersbridge.isegoria.auth.signup

data class SignUpUser (var givenName: String,
                      var familyName: String,
                      var gender: String,
                      var nationality: String,
                      var yearOfBirth: String,
                      var email: String,
                      var password: String,
                      var institutionName: String?,
                      var institutionId: Long,
                      var accountVerified: Boolean = false,
                      var hasPersonality: Boolean = false)
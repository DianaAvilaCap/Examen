package com.codigo.ms_security.service;

import com.codigo.ms_security.aggregates.request.SignInRequest;
import com.codigo.ms_security.aggregates.response.SignInResponse;

public interface AuthenticationService {

    //Metodos de autenticacion
    SignInResponse signInResponse(SignInRequest signInRequest);

    boolean validarToken(String token);
}

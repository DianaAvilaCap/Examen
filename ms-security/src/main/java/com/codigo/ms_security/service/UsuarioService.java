package com.codigo.ms_security.service;

import com.codigo.ms_security.aggregates.request.SignUpRequest;
import com.codigo.ms_security.entity.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UsuarioService {

    UserDetailsService userDetailsService();
    Usuario signUpUser(SignUpRequest signUpRequest);
    List<Usuario> getInfoUser();
    Usuario getByEmail(String email);
}

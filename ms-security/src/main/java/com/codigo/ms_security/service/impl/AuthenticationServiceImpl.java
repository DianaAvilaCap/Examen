package com.codigo.ms_security.service.impl;

import com.codigo.ms_security.aggregates.request.SignInRequest;
import com.codigo.ms_security.aggregates.response.SignInResponse;
import com.codigo.ms_security.repository.RolRepository;
import com.codigo.ms_security.repository.UsuarioRepository;
import com.codigo.ms_security.service.AuthenticationService;
import com.codigo.ms_security.service.JwtService;
import com.codigo.ms_security.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Override
    public SignInResponse signInResponse(SignInRequest signInRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signInRequest.getEmail(),signInRequest.getPassword()
        ));
        var user = usuarioRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("usuario no encontrado en base de datos"));
        var token = jwtService.generateToken(user);
        return SignInResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public boolean validarToken(String token) {
        final String jwt;
        final String userEmail;

        if(Objects.nonNull(token) && !token.isEmpty()){
            jwt = token.substring(7);
            userEmail = jwtService.extractUsername(jwt);
            if(Objects.nonNull(userEmail) && !userEmail.isEmpty()){
                UserDetails userDetails = usuarioService.userDetailsService().loadUserByUsername(userEmail);
                return jwtService.validateToken(jwt, userDetails);
            }
        }

        return false;
    }

}

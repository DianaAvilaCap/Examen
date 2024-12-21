package com.codigo.ms_security.config;

import com.codigo.ms_security.service.JwtService;
import com.codigo.ms_security.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String tokenExtraidoHeader = request.getHeader("Authorization");
        final String tokenLimpio;
        final String userEmail;

        //Validar el encabezado de la solicitud | Validar token
        if(!StringUtils.hasText(tokenExtraidoHeader)
            || !StringUtils.startsWithIgnoreCase(tokenExtraidoHeader,"Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        //Limpiamos el token de la palabra bearer
        tokenLimpio = tokenExtraidoHeader.substring(7);
        //Extraemos el usuario (username) del token
        userEmail = jwtService.extractUsername(tokenLimpio);

        //Validamos si el usuario no es nulo y no se encuentra autenticado
        if(Objects.nonNull(userEmail)
            && SecurityContextHolder.getContext().getAuthentication() == null){

            //Definir un contexto de seguridad vacio
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            //Recuperando los detalles del usuario desde base de datos
            UserDetails userDetails = usuarioService.userDetailsService().loadUserByUsername(userEmail);

            //Validamos que el token no este expirado y que pertenezca al usuario
            if(jwtService.validateToken(tokenLimpio, userDetails)){
                //Creamos un token de autenticacion a través de UsernamePasswordAuthenticationToken
                //-> Se requiere colocar los detalles del usuario, credenciales, roles / permisos
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                //Asignando los detalles de la solicitud (del request al token de autenticacion)
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //Asignar la autenticacion a mi contexto creado
                securityContext.setAuthentication(authenticationToken);
                //Asigno mi contexto de seguridad al Holder de security
                SecurityContextHolder.setContext(securityContext);

            }

        }

        //Continua la solicitud
        filterChain.doFilter(request,response);

    }
}

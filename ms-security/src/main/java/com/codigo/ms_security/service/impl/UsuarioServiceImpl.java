package com.codigo.ms_security.service.impl;

import com.codigo.ms_security.aggregates.constants.Constants;
import com.codigo.ms_security.aggregates.request.SignUpRequest;
import com.codigo.ms_security.entity.Rol;
import com.codigo.ms_security.entity.Role;
import com.codigo.ms_security.entity.Usuario;
import com.codigo.ms_security.repository.RolRepository;
import com.codigo.ms_security.repository.UsuarioRepository;
import com.codigo.ms_security.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Override
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return usuarioRepository.findByEmail(username)
                        .orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado en base de datos"));
            }
        };
    }

    @Override
    public Usuario signUpUser(SignUpRequest signUpRequest) {
        //Validar si existe usuario
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(signUpRequest.getEmail());

        if(usuarioExistente.isPresent()){
            throw new IllegalArgumentException("El usuario con email " + signUpRequest.getEmail() + " ya está registrado.");
        }

        Usuario usuario = getUsuarioEntity(signUpRequest);
        Set<Rol> roles = signUpRequest.getRoles().stream()
                .map(roleRequest -> {
                    // Obtener Rol
                    return getRoles(Role.valueOf(roleRequest.getNombreRol()));
                })
                .collect(Collectors.toSet());
        usuario.setRoles(roles);
        return usuarioRepository.save(usuario);
    }

    @Override
    public List<Usuario> getInfoUser() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario getByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("Usuario no encontrado en base de datos"));
    }

    private Rol getRoles(Role rolBuscado){
        return rolRepository.findByNombreRol(rolBuscado.name())
                .orElseThrow(
                        () -> new RuntimeException(
                                "ERROR EL ROL NO EXISTE :" + rolBuscado.name()));
    }

    private Usuario getUsuarioEntity(SignUpRequest signUpRequest){
        return Usuario.builder()
                .nombres(signUpRequest.getNombres())
                .apellidos(signUpRequest.getApellidos())
                .email(signUpRequest.getEmail())
                .password(new BCryptPasswordEncoder().encode(signUpRequest.getPassword()))
                .tipoDoc(signUpRequest.getTipoDoc())
                .numDoc(signUpRequest.getNumDoc())
                .isAccountNonExpired(Constants.STATUS_ACTIVE)
                .isAccountNonLocked(Constants.STATUS_ACTIVE)
                .isCredentialsNonExpired(Constants.STATUS_ACTIVE)
                .isEnabled(Constants.STATUS_ACTIVE)
                .build();
    }
}

package com.codigo.ms_security.controller;

import com.codigo.ms_security.aggregates.request.SignUpRequest;
import com.codigo.ms_security.entity.Usuario;
import com.codigo.ms_security.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UsuarioService usuarioService;

    @PostMapping()
    public ResponseEntity<Usuario> createUser(@RequestBody SignUpRequest signUpRequest){
        return ResponseEntity.ok(usuarioService.signUpUser(signUpRequest));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Usuario> getUser(@PathVariable String username){
        return ResponseEntity.ok(usuarioService.getByEmail(username));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Usuario>> getAll(){
        return ResponseEntity.ok(usuarioService.getInfoUser());
    }
}

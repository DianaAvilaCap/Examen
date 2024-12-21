package com.codigo.ms_security.service.impl;

import com.codigo.ms_security.aggregates.constants.Constants;
import com.codigo.ms_security.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Log4j2
public class JwtServiceImpl implements JwtService {

    @Value("${key.signature}")
    private String keySignature;

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setClaims(addClaim(userDetails))
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 120000))
                .claim("userCreater",Constants.USER_ADMIN)
                .claim("type",Constants.ACCESS)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token);
    }

    //Generar métodos de apoyo para interacturar o generar el token

    //Método para generar un key para firmar los tokens
    private Key getSignKey(){
        log.info("Clave para firmar: "+keySignature);
        byte[] key = Decoders.BASE64.decode(keySignature);
        log.info("Key con la que vamos a firmar: "+ Keys.hmacShaKeyFor(key));
        return Keys.hmacShaKeyFor(key);
    }

    //Metodo para extraer el payload (claims) del token
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder().setSigningKey(getSignKey()).build()
                .parseClaimsJws(token).getBody();
    }

    //Metodo para obtener un atributo del payload
    private <T> T extractClaim(String token, Function<Claims, T> claimsTFunction){
        //final Claims claims = extractAllClaims(token);
        //return claimsTFunction.apply(claims);
        return claimsTFunction.apply(extractAllClaims(token));
    }

    //Metodo para validar si el token esta expirado
    private boolean isTokenExpired(String token){
        return extractClaim(token,Claims::getExpiration).before(new Date());
    }

    //Claims personalizados
    private Map<String, Object> addClaim(UserDetails userDetails){
        Map<String,Object> claims = new HashMap<>();
        claims.put(Constants.CLAVE_AccountNonLocked, userDetails.isAccountNonLocked());
        claims.put(Constants.CLAVE_AccountNonExpired, userDetails.isAccountNonExpired());
        claims.put(Constants.CLAVE_CredentialsNonExpired, userDetails.isCredentialsNonExpired());
        claims.put(Constants.CLAVE_Enabled, userDetails.isEnabled());
        return claims;
    }

}

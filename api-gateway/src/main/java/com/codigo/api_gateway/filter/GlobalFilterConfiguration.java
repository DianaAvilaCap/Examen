package com.codigo.api_gateway.filter;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class GlobalFilterConfiguration {

    @Component
    public class AuthenticationFilter implements GlobalFilter, Ordered{

        //Inyecciones
        @Autowired
        private WebClient.Builder webClientBuilder;

        @Autowired
        private EurekaClient eurekaClient;

        //Definir las rutas que van a ser excluidas
        private final AntPathMatcher antPathMatcher = new AntPathMatcher();
        //Rutas que se van a excluir de la validacion
        private static final List<String> EXCLUDE_PATHS = List.of(
            "/api/v1/authentication/**"
        );

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

            String path = exchange.getRequest().getURI().getPath();

            //Verificamos si la ruta o path esta en
            //la lista de excluidos para la validacion
            if(isExcludePath(path)){
                return chain.filter(exchange);
            }

            //Obtener el token de la solicitud | Token debe llegar puro o sea sin bearer
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");

            //Validamos que el token no sea nulo o vacio
            if(token == null || token.isEmpty()){
                return Mono.error(new IllegalArgumentException("Token requerido para la operación"));
            }

            //Obtener url completa usando el eureka serve y el endopoint que se ejecuta
            String serviceUrl = getServiceUrl("ms-security", "/api/v1/authentication/validateToken");

            //Ejecutamos el servicio de validacion
            return webClientBuilder.build()
                    .post()
                    .uri(serviceUrl)
                    .header("validate", token)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .flatMap(isValid -> {
                        if(Boolean.TRUE.equals(isValid)){
                            return chain.filter(exchange);
                        }else{
                            return Mono.error(new IllegalArgumentException("Token no válido"));
                        }
                    })
                    .onErrorResume(erro -> Mono.error(new RuntimeException("Error al validar el token de acceso"))
                    );
        }

        private boolean isExcludePath(String path){
            return EXCLUDE_PATHS.stream().anyMatch(
                    pattern -> antPathMatcher.match(pattern,path));
        }

        private String getServiceUrl(String serviceName, String endPoint){
            InstanceInfo instanceInfo = eurekaClient.getNextServerFromEureka(serviceName, false);
            return instanceInfo.getHomePageUrl() + endPoint;
        }

        @Override
        public int getOrder() {
            return 0;//Menor valor == Mayor prioridad || Mayor valor == Menor Prioridad
        }
    }
}

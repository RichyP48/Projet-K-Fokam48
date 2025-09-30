package com.mogou.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

//@Component
public class TokenFilter extends AbstractGatewayFilterFactory<TokenFilter.Config> {
    private static  final String SECRET="b9f867ca6f7f9b081f154de73bff0791ca3dc4841c77e8d09399cc268b9c47f1e12cb658c380733b0b805545920cc4ffed2418c60ec4df9409ed0536eb333e7e";

    public TokenFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            if(path.equals("/user/login")|| path.equals("/user/register")){
                return chain.filter(exchange.mutate().request(r->r.header("X-Secret-Key","SECRET")).build());
            }
            HttpHeaders header=exchange.getRequest().getHeaders();
            if(!header.containsKey(HttpHeaders.AUTHORIZATION)){
                throw new RuntimeException("Authorization header not present");
            }
            String authHeader=header.getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader==null||!authHeader.startsWith("Bearer ")){
                throw new RuntimeException("Authorization header not present");
            }
            String token=authHeader.substring(7);
            try{
                Claims claims= Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
                exchange=exchange.mutate().request(r->r.header("X-Secret-Key","SECRET")).build();
            }catch (Exception e){
                throw new RuntimeException("Invalid token");
            }
            return chain.filter(exchange);
        };

    }
    public static class Config{}
}

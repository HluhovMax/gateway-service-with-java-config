package com.example.gatewayservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayServiceApplication {

//	@Autowired
//	@Qualifier("customGatewayFilter")
//	private GatewayFilter gatewayFilter;

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				.route("client-service", r -> r
						.path("/api/v1/client/**")
						.filters(f -> f.requestRateLimiter().rateLimiter(RedisRateLimiter.class,
								c -> c.setBurstCapacity(10).setReplenishRate(20))
										.configure(c -> c.setKeyResolver(exchange -> Mono.just(exchange.getSession().subscribe().toString())))
								/**.filter(gatewayFilter)*/)
						.uri("lb://client-service"))
				.route("auth-service", r -> r
						.path("/v1/oauth/token")
						.uri("lb://auth-service"))
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}

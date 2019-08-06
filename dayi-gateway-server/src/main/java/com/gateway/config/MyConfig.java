package com.gateway.config;


import com.gateway.filter.MyGateWayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyConfig {

    /**
     * 在路由中添加自定义的过滤器
     * @param routeLocatorBuilder
     * @return
     */
    @Bean
    public RouteLocator getRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        RouteLocatorBuilder.Builder route = routeLocatorBuilder.routes().route(
                r -> r.path("/api/client/**")
                        .filters(f -> f.stripPrefix(2).filter(getMyFilter()))//在路由中添加自定义的过滤器
                        .uri("lb://gateway-client")
                        .order(100)
                        .id("gateway-client1")
        );
        return route.build();
    }

    @Bean
    public MyGateWayFilter getMyFilter(){
        return new MyGateWayFilter();
    }



}

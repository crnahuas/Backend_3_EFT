package cl.duoc.bancoxyz.bffmobile.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean
    @Primary
    RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder(BearerTokenRelay relay) {
        return RestClient.builder().requestInterceptor(relay);
    }
}

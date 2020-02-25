package example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class PassController {

    /*@Bean
    ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            BasicHeaderElementIterator it = new BasicHeaderElementIterator(
                    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (NumberFormatException exception) {
                        exception.printStackTrace();
                    }
                }
            }
            // If there is no Keep-Alive header. Keep the connection for 60 seconds
            return 60 * 1000;
        };
    }
    @Bean
    @Qualifier("apacheRestTemplate")
    public ClientHttpRequestFactory createRequestFactory() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(100);
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .build();
        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    private RestTemplate restTemplate;

    @Autowired
    public PassController() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(createRequestFactory());
    }
*/
    WebClient webClient;
    @Value("${upstream}")
    private String upstream;

    public PassController() {
        WebClient.Builder webClientBuilderInstance = WebClient
                .builder()
                .baseUrl(upstream);
        webClient = webClientBuilderInstance.build();
    }

    @RequestMapping("/healthz")
    public Mono<String> healthz(ServerHttpRequest request) {
        return Mono.just("{\"app\": \"Go-gitter\"}");
    }

    @RequestMapping("/**")
    public Mono<String> pass(ServerHttpRequest request) {
        String path = request.getPath().toString();
        return webClient.get().uri(path).retrieve().bodyToMono(String.class);
    }


}

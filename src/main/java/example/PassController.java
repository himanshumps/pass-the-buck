package example;

import org.apache.http.HeaderElement;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PassController {

    @Bean
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

    @Value("${upstream}")
    private String upstream;

    @RequestMapping("/healthz")
    public String healthz(HttpServletRequest request) {
        return "{\"app\": \"Go-gitter\"}";
    }

    @RequestMapping("/**")
    public String pass(HttpServletRequest request) {
        String path = request.getServletPath();
        ResponseEntity<String> response = requestUpstream(path);
        return response.getBody();
    }

    private ResponseEntity<String> requestUpstream(String path) {
        String resourceUrl = upstream + path;
        return restTemplate.getForEntity(resourceUrl, String.class);
    }

}

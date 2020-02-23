package example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PassController {

    private RestTemplate restTemplate;

    @Autowired
    public PassController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @Value("${upstream}")
    private String upstream;

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

package br.com.test.graintransport.grain_transport_api.filter;

import br.com.test.graintransport.grain_transport_api.repository.BalancaRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class ApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final String PROTECTED_PATH = "/api/ingest";

    private final BalancaRepository balancaRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (path.startsWith(PROTECTED_PATH)) {
            String apiKey = httpRequest.getHeader(API_KEY_HEADER);

            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Requisição sem X-Api-Key rejeitada: path={}", path);
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"X-Api-Key header é obrigatório\"}");
                return;
            }

            boolean keyValida = balancaRepository.findByApiKey(apiKey).isPresent();
            if (!keyValida) {
                log.warn("X-Api-Key inválida rejeitada: key={}", apiKey);
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"X-Api-Key inválida ou não cadastrada\"}");
                return;
            }

            log.debug("X-Api-Key válida: key={}", apiKey);
        }

        chain.doFilter(request, response);
    }
}

package org.example.authenticationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.example.authenticationservice.entity.Roles;
import org.example.authenticationservice.security.JwtTokenService;
import org.example.authenticationservice.service.PolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ForwardAuthController {

    private final JwtTokenService jwt;
    private final PolicyService policy;

    @Operation(
            summary = "Validează autentificarea pentru API Gateway (Forward Auth)",
            description = "Acesta este un endpoint intern folosit de API Gateway (ex. Traefik) pentru a valida token-ul JWT " +
                    "și a autoriza cererile către alte microservicii. Nu este destinat apelării directe de către client.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "app-auth", description = "Token-ul JWT al utilizatorului", required = true, schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.HEADER, name = "X-Forwarded-Method", description = "Metoda HTTP originală a cererii (ex. GET, POST)", required = true, schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.HEADER, name = "X-Forwarded-Uri", description = "URI-ul original al cererii (ex. /api/device/1)", required = true, schema = @Schema(type = "string"))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cerere validă și autorizată (inclusiv pentru OPTIONS)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat - Token 'app-auth' lipsește sau este invalid", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis - Token-ul este valid, dar utilizatorul nu are rolul necesar pentru resursă", content = @Content)
    })
    @RequestMapping(path = "/validate", method = { RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public void forwardAuth(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpStatus.OK.value());
            return;
        }

        String token = req.getHeader("app-auth");
        if (!StringUtils.hasText(token)) {
            res.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing token");
            return;
        }

        var auth = jwt.getAuthenticationFromToken(token);

        Set<Roles> roles = auth.getAuthorities().stream()
                .map(a -> Roles.valueOf(a.getAuthority().replace("ROLE_", "")))
                .collect(java.util.stream.Collectors.toSet());

        String method = headerOrDefault(req, "X-Forwarded-Method", req.getMethod());
        String uri    = headerOrDefault(req, "X-Forwarded-Uri",    req.getRequestURI());
        String path = uri.split("\\?")[0];

        boolean allowed = policy.isAllowed(method, path, roles);
        if (!allowed) {
            res.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden by policy");
            return;
        }

        res.setStatus(HttpStatus.OK.value());
    }

    private static String headerOrDefault(HttpServletRequest req, String name, String fallback) {
        String v = req.getHeader(name);
        return StringUtils.hasText(v) ? v : fallback;
    }
}
package org.example.authenticationservice.service;

import org.example.authenticationservice.entity.Rule;
import org.example.authenticationservice.entity.Roles;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;

@Component
public class PolicyService {

    private final AntPathMatcher ant = new AntPathMatcher();
    private final List<Rule> rules = List.of(
            // --- ADMIN-only: colecția /api/devices (findAll)
            new Rule("GET",    "/api/device",          Set.of(Roles.ADMIN)),
            new Rule("GET",    "/api/device/",         Set.of(Roles.ADMIN)),

            // --- ADMIN-only: create device
            new Rule("POST",   "/api/device",          Set.of(Roles.ADMIN)),
            new Rule("POST",   "/api/device/",         Set.of(Roles.ADMIN)),

            // --- ADMIN-only: user management subpaths (/user, /user/{id})
            new Rule("POST",   "/api/device/user",     Set.of(Roles.ADMIN)),
            new Rule("POST",   "/api/device/user/",    Set.of(Roles.ADMIN)),
            new Rule("DELETE", "/api/device/user/*",   Set.of(Roles.ADMIN)),
            // (defensiv, chiar dacă nu ai GET pe /user)
            new Rule("GET",    "/api/device/user",     Set.of(Roles.ADMIN)),
            new Rule("GET",    "/api/device/user/*",   Set.of(Roles.ADMIN)),

            // --- ADMIN-only: update/delete device by id
            new Rule("PUT",    "/api/device/*",        Set.of(Roles.ADMIN)),
            new Rule("DELETE", "/api/device/*",        Set.of(Roles.ADMIN)),

            // --- USER + ADMIN: doar GET /api/devices/{id} (getDevicesByUserId)
            // (vinea DUPĂ regulile admin-only de mai sus ca să nu „scape” /user/*)
            new Rule("GET",    "/api/device/*",        Set.of(Roles.USER, Roles.ADMIN)),

            // --- CATCH-ALL sub /api/devices/** -> ADMIN-only (orice endpoint viitor)
            new Rule("*",      "/api/device/**",       Set.of(Roles.ADMIN)),
            new Rule("GET",    "/api/user",    Set.of(Roles.ADMIN)),
            new Rule("GET",    "/api/user/",   Set.of(Roles.ADMIN)),

            new Rule("POST",   "/api/user",    Set.of(Roles.ADMIN)),
            new Rule("POST",   "/api/user/",   Set.of(Roles.ADMIN)),

            new Rule("DELETE", "/api/user/*",  Set.of(Roles.ADMIN)),
            new Rule("PUT",    "/api/user/*",  Set.of(Roles.ADMIN)),

            // --- CATCH-ALL: orice alt endpoint sub /api/users/** e tot ADMIN-only
            new Rule("*",      "/api/user/**", Set.of(Roles.ADMIN)),

            new Rule("POST",      "/api/monitoring/device", Set.of(Roles.ADMIN)),
            new Rule("DELETE",      "/api/monitoring/device/*", Set.of(Roles.ADMIN)),
            new Rule("*",      "/api/monitoring/device/**", Set.of(Roles.ADMIN)),

            new Rule("GET",    "/api/monitoring",           Set.of(Roles.USER, Roles.ADMIN)),
            new Rule("GET",    "/api/monitoring/",          Set.of(Roles.USER, Roles.ADMIN)),
            new Rule("GET",    "/api/monitoring/**",        Set.of(Roles.USER, Roles.ADMIN)),
            new Rule("*",      "/api/monitoring/**",        Set.of(Roles.ADMIN))
    );

    public boolean isAllowed(String method, String uri, Set<Roles> userRoles) {
        for (Rule r : rules) {
            boolean methodOk = r.getHttpMethod().equals("*") || r.getHttpMethod().equalsIgnoreCase(method);
            boolean pathOk   = ant.match(r.getPathPattern(), uri);
            if (methodOk && pathOk) {
                return userRoles.stream().anyMatch(r.getAllowed()::contains);
            }
        }
        return false;
    }
}

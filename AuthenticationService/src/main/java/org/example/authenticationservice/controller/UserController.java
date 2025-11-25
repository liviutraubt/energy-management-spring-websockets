package org.example.authenticationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.authenticationservice.dto.LoginRequest;
import org.example.authenticationservice.dto.RegisterRequest;
import org.example.authenticationservice.dto.UserDTO;
import org.example.authenticationservice.security.JwtTokenService;
import org.example.authenticationservice.security.annotations.AllowAdmin;
import org.example.authenticationservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private  final JwtTokenService jwtTokenService;

    @PostMapping("/register")
    @Operation(summary = "Înregistrează un utilizator nou", description = "Creează un cont nou pentru un utilizator standard (rol 'CLIENT').")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator înregistrat cu succes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Date de înregistrare invalide (ex. username-ul există deja)",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)) })
    })
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try{
            return ResponseEntity.ok(userService.registerUser(registerRequest));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/healthcheck")
    @Operation(summary = "Verifică starea de sănătate a serviciului", description = "Endpoint simplu pentru a verifica dacă serviciul este pornit și rulează.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviciul este activ",
                    content = @Content)
    })
    public ResponseEntity<?> checkHealth() {
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Autentifică un utilizator", description = "Autentifică utilizatorul pe baza username-ului și parolei. Returnează un token JWT și setează un 'auth-cookie'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autentificare reușită",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"token\": \"jwt-token-string\"}")) }),
            @ApiResponse(responseCode = "401", description = "Date de autentificare invalide",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Invalid credentials\"}")) })
    })
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpServletResponse response) {
        UserDTO user = userService.login(request);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","Invalid credentials"));
        }

        String jwt = jwtTokenService.createJwtToken(user.username(), user.role(), user.id());
        Cookie cookie = new Cookie("auth-cookie",jwt);
        cookie.setPath("/");
        cookie.setDomain("localhost");
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @GetMapping("/getall")
    @AllowAdmin
    @Operation(summary = "Obține toți utilizatorii (Doar Admin)", description = "Returnează o listă cu toți utilizatorii înregistrați. Necesită rol de ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth")) // Presupunând că ai configurat securitatea bearer
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de utilizatori a fost returnată",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserDTO.class))) }),
            @ApiResponse(responseCode = "401", description = "Neautorizat (fără token)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis (nu este ADMIN)", content = @Content)
    })
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userList = userService.getUsers();
        return ResponseEntity.ok(userList);
    }

    @DeleteMapping("/{id}")
    @AllowAdmin
    @Operation(summary = "Șterge un utilizator (Doar Admin)", description = "Șterge un utilizator pe baza ID-ului. Necesită rol de ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizatorul a fost șters",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Ștergerea a eșuat (ex. utilizatorul nu a fost găsit)",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)) }),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try{
            return ResponseEntity.ok(userService.deleteUser(id));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register-admin")
    @AllowAdmin
    @Operation(summary = "Înregistrează un nou admin (Doar Admin)", description = "Creează un nou utilizator cu rol de ADMIN. Necesită rol de ADMIN.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator admin înregistrat cu succes",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Date de înregistrare invalide",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)) }),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        try{
            return ResponseEntity.ok(userService.adminRegisterUser(registerRequest));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.userservice.dto.UserDTO;
import org.example.userservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "User Data Management", description = "API pentru gestionarea datelor utilizatorilor (Nume, Adresă, Email etc.)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obține toți utilizatorii (Doar Admin)", description = "Returnează o listă cu detaliile tuturor utilizatorilor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de utilizatori",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<List<UserDTO>> findAllUsers() {
        List<UserDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Creează detaliile unui utilizator (Doar Admin)", description = "Creează intrarea de detalii pentru un utilizator (Nume, Email etc.). ID-ul trebuie să corespundă celui din AuthenticationService.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator creat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "1"))),
            @ApiResponse(responseCode = "400", description = "Date invalide (ex. email invalid, ID-ul lipsește)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> saveUser(@RequestBody UserDTO userDTO) {
        try{
            return ResponseEntity.ok(userService.insertUser(userDTO));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Șterge detaliile unui utilizator (Doar Admin)", description = "Șterge detaliile unui utilizator pe baza ID-ului.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator șters", content = @Content),
            @ApiResponse(responseCode = "400", description = "Eroare (ex. utilizatorul nu a fost găsit)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try{
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizează detaliile unui utilizator (Doar Admin)", description = "Actualizează detaliile (Nume, Email, Adresă etc.) pentru un utilizator existent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator actualizat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "1"))),
            @ApiResponse(responseCode = "400", description = "Eroare (ex. utilizatorul nu a fost găsit sau datele sunt invalide)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try{
            return ResponseEntity.ok(userService.updateUser(id, userDTO));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package org.example.deviceservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.deviceservice.dto.DeviceDTO;
import org.example.deviceservice.dto.UserDTO;
import org.example.deviceservice.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device")
@Tag(name = "Device Management", description = "API pentru gestionarea dispozitivelor și a mapărilor lor către utilizatori")
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    @Operation(summary = "Obține toate dispozitivele (Doar Admin)", description = "Returnează o listă cu toate dispozitivele din sistem.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de dispozitive",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<List<DeviceDTO>> findAll() {
        List<DeviceDTO> devices = deviceService.findAll();
        return ResponseEntity.ok(devices);
    }

    @PostMapping
    @Operation(summary = "Creează un dispozitiv nou (Doar Admin)", description = "Adaugă un dispozitiv nou și îl asociază unui utilizator existent.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv creat cu succes",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "1"))),
            @ApiResponse(responseCode = "400", description = "Date invalide (ex. utilizatorul specificat nu există)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> saveDevice(@RequestBody DeviceDTO deviceDTO) {
        try{
            return ResponseEntity.ok(deviceService.insertDevice(deviceDTO));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/user")
    @Operation(summary = "Creează entitatea utilizator locală (Doar Admin)", description = "Endpoint intern pentru a crea o 'umbră' a utilizatorului în acest serviciu, necesară pentru maparea dispozitivelor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator creat local",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "1"))),
            @ApiResponse(responseCode = "400", description = "Utilizatorul există deja",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> insertUser(@RequestBody UserDTO userDTO) {
        try{
            return ResponseEntity.ok(deviceService.insertUser(userDTO));
        }
        catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "Șterge entitatea utilizator locală (Doar Admin)", description = "Endpoint intern pentru a șterge 'umbra' utilizatorului și dispozitivele asociate acestuia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator și dispozitivele asociate șterse local", content = @Content),
            @ApiResponse(responseCode = "400", description = "Eroare (ex. utilizatorul nu a fost găsit)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try{
            deviceService.deleteUser(id);
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Șterge un dispozitiv (Doar Admin)", description = "Șterge un dispozitiv specific pe baza ID-ului său.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv șters", content = @Content),
            @ApiResponse(responseCode = "400", description = "Eroare (ex. dispozitivul nu a fost găsit)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> deleteDevice(@PathVariable Long id) {
        try{
            deviceService.deleteDevice(id);
            return ResponseEntity.ok().build();
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizează un dispozitiv (Doar Admin)", description = "Actualizează detaliile unui dispozitiv existent, inclusiv utilizatorul asociat.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv actualizat",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class, example = "1"))),
            @ApiResponse(responseCode = "400", description = "Eroare (ex. dispozitivul nu a fost găsit)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<?> updateDevice(@PathVariable Long id, @RequestBody DeviceDTO deviceDTO) {
        try{
            return ResponseEntity.ok(deviceService.updateDevice(deviceDTO, id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obține dispozitivele unui utilizator", description = "Returnează o listă cu toate dispozitivele asociate unui ID de utilizator specific.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de dispozitive a utilizatorului",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acces interzis", content = @Content)
    })
    public ResponseEntity<List<DeviceDTO>> findDeviceByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(id));
    }
}

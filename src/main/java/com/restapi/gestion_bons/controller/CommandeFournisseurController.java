package com.restapi.gestion_bons.controller;

import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurCreateDTO;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurResponseDTO;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurUpdateDTO;
import com.restapi.gestion_bons.service.commandeFournisseur.CommandeFournisseurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/commandes")
public class CommandeFournisseurController {

    private final CommandeFournisseurService commandeFournisseurService;

    public CommandeFournisseurController(CommandeFournisseurService commandeFournisseurService) {
        this.commandeFournisseurService = commandeFournisseurService;
    }

    @GetMapping
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> listAll(){
        List<CommandeFournisseurResponseDTO> commandes = commandeFournisseurService.findAll();
        return ResponseEntity.ok(commandes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponseDTO> getCommandeFournisseurById(@PathVariable Long id){
        CommandeFournisseurResponseDTO commandeFournisseur = commandeFournisseurService.findById(id);
        return ResponseEntity.ok(commandeFournisseur);
    }

    @PostMapping
    public ResponseEntity<CommandeFournisseurResponseDTO> createCommandeFournisseur(
            @RequestBody @Valid
            CommandeFournisseurCreateDTO createDTO){
        CommandeFournisseurResponseDTO created = commandeFournisseurService.save(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeFournisseurResponseDTO> updateCommandeFournisseur(
            @PathVariable Long id,
            @Valid @RequestBody CommandeFournisseurUpdateDTO fournisseurUpdateDTO
            ){
        CommandeFournisseurResponseDTO updated = commandeFournisseurService.update(id, fournisseurUpdateDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommandeFournisseur(
            @PathVariable Long id
    ){
        commandeFournisseurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fournisseur/{id}")
    public ResponseEntity<List<CommandeFournisseurResponseDTO>> getCommandeByFournisseurId(
            @PathVariable Long id
    ){
        List<CommandeFournisseurResponseDTO> commandes = commandeFournisseurService.findByFournisseurId(id);
        return ResponseEntity.ok(commandes);
    }

    @PutMapping("/{id}/reception")
    public ResponseEntity<CommandeFournisseurResponseDTO> receptionnerCommande(
            @PathVariable Long id
    ){
        CommandeFournisseurResponseDTO commandeFournisseur = commandeFournisseurService.receptionnerCommande(id);
        return ResponseEntity.ok(commandeFournisseur);
    }

    @PutMapping("/{id}/valide")
    public ResponseEntity<CommandeFournisseurResponseDTO> valideCommande(
            @PathVariable Long id
    ){
        CommandeFournisseurResponseDTO commandeFournisseur = commandeFournisseurService.valideCommande(id);
        return ResponseEntity.ok(commandeFournisseur);
    }
}

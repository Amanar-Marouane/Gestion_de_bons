package com.restapi.gestion_bons.controller;

import com.restapi.gestion_bons.contracts.BonDeSortieContract;
import com.restapi.gestion_bons.dao.BonDeSortieDAO;
import com.restapi.gestion_bons.dao.BonDeSortieLigneDAO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieCreateDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieLigneDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieResponseDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieUpdateDTO;
import com.restapi.gestion_bons.entitie.BonDeSortie;
import com.restapi.gestion_bons.entitie.BonDeSortieLigne;
import com.restapi.gestion_bons.mapper.BonDeSortieLigneMapper;
import com.restapi.gestion_bons.mapper.BonDeSortieMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/bons-sortie")
@RequiredArgsConstructor
public class BonDeSortieController {

    private final BonDeSortieContract bonDeSortieService;
    private final BonDeSortieDAO bonDeSortieDAO;
    private final BonDeSortieLigneDAO bonDeSortieLigneDAO;
    private final BonDeSortieLigneMapper bonDeSortieLigneMapper;
    private final BonDeSortieMapper bonDeSortieMapper;


    @GetMapping
    public ResponseEntity<List<BonDeSortieResponseDTO>> listAll() {
        return ResponseEntity.ok(bonDeSortieService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BonDeSortieResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.findById(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BonDeSortieResponseDTO> create(@RequestBody @Valid BonDeSortieCreateDTO dto) {
        BonDeSortieResponseDTO created = bonDeSortieService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BonDeSortieResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid BonDeSortieUpdateDTO dto) {
        return ResponseEntity.ok(bonDeSortieService.update(id, dto));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<BonDeSortieResponseDTO> valider(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.valider(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<BonDeSortieResponseDTO> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(bonDeSortieService.annuler(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bonDeSortieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/numero/{numeroBon}")
    public ResponseEntity<BonDeSortieResponseDTO> getByNumeroBon(@PathVariable String numeroBon) {
        return ResponseEntity.ok(bonDeSortieService.findByNumeroBon(numeroBon));
    }

    @GetMapping("/atelier/{atelierId}")
    public ResponseEntity<List<BonDeSortieResponseDTO>> getByAtelierId(@PathVariable Long atelierId) {
        return ResponseEntity.ok(bonDeSortieService.findByAtelierId(atelierId));
    }
}

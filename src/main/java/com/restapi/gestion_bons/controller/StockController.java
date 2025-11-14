package com.restapi.gestion_bons.controller;

import com.restapi.gestion_bons.contracts.StockContract;
import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.dto.stock.*;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockContract stockService;

    @GetMapping
    public ResponseEntity<List<StockGlobalDTO>> getStockGlobal() {
        return ResponseEntity.ok(stockService.getStockGlobal());
    }

    @GetMapping("/produit/{id}")
    public ResponseEntity<StockProduitDetailDTO> getStockByProduitId(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getStockByProduitId(id));
    }

    @GetMapping("/mouvements")
    public ResponseEntity<List<MouvementStockResponseDTO>> getMouvements() {
        return ResponseEntity.ok(stockService.getMouvements());
    }

    @GetMapping("/mouvements/produit/{id}")
    public ResponseEntity<List<MouvementStockResponseDTO>> getMouvementsByProduitId(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsByProduitId(id));
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<StockAlertDTO>> getAlertes() {
        return ResponseEntity.ok(stockService.getAlertes());
    }

    @GetMapping("/valorisation")
    public ResponseEntity<StockValorisationDTO> getValorisation() {
        return ResponseEntity.ok(stockService.getValorisation());
    }

    @GetMapping("/mouvements/lot/{id}")
    public ResponseEntity<List<MouvementStockResponseDTO>> getMouvementsByLotId(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsByLotId(id));
    }

    @GetMapping("/mouvements/type/sortie") // by mouvement type
    public ResponseEntity<List<MouvementStockResponseDTO>> getSortieMouvements() {
        return ResponseEntity.ok(stockService.getMouvementsByTypeMouvement(TypeMouvement.SORTIE));
    }

    @GetMapping("/mouvements/type/entree") // by mouvement type
    public ResponseEntity<List<MouvementStockResponseDTO>> getEntreeMouvements() {
        return ResponseEntity.ok(stockService.getMouvementsByTypeMouvement(TypeMouvement.ENTREE));
    }

    @GetMapping("/mouvements/date")
    public ResponseEntity<List<MouvementStockResponseDTO>> getMouvementsByDateInterval(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        System.out.println("START = " + start);
        System.out.println("END = " + end);

        return ResponseEntity.ok(stockService.getMouvementsByDateInterval(start, end));
    }

}

package com.restapi.gestion_bons.controller;

import com.restapi.gestion_bons.contracts.StockContract;
import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.dto.stock.*;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public ResponseEntity<Page<MouvementStockResponseDTO>> getMouvements(
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) Long lotId,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity
                .ok(stockService.getMouvementsByCriteria(produitId, lotId, type, startDate, endDate, pageable));
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<StockAlertDTO>> getAlertes() {
        return ResponseEntity.ok(stockService.getAlertes());
    }

    @GetMapping("/valorisation")
    public ResponseEntity<StockValorisationDTO> getValorisation() {
        return ResponseEntity.ok(stockService.getValorisation());
    }
}

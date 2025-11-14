package com.restapi.gestion_bons.contracts;

import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.dto.stock.*;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;

import java.time.LocalDateTime;
import java.util.List;

public interface StockContract {
    List<StockGlobalDTO> getStockGlobal();

    StockProduitDetailDTO getStockByProduitId(Long produitId);

    List<MouvementStockResponseDTO> getMouvements();

    List<MouvementStockResponseDTO> getMouvementsByProduitId(Long produitId);

    List<MouvementStockResponseDTO> getMouvementsByLotId(Long lotId);

    List<MouvementStockResponseDTO> getMouvementsByTypeMouvement(TypeMouvement type);

    List<MouvementStockResponseDTO> getMouvementsByDateInterval(LocalDateTime start, LocalDateTime end);

    List<StockAlertDTO> getAlertes();

    StockValorisationDTO getValorisation();
}

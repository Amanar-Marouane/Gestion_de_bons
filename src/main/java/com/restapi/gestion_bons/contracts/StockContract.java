package com.restapi.gestion_bons.contracts;

import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.dto.stock.*;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface StockContract {
    List<StockGlobalDTO> getStockGlobal();

    StockProduitDetailDTO getStockByProduitId(Long produitId);

    Page<MouvementStockResponseDTO> getMouvementsByCriteria(
            Long produitId,
            Long lotId,
            TypeMouvement type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);

    List<StockAlertDTO> getAlertes();

    StockValorisationDTO getValorisation();
}

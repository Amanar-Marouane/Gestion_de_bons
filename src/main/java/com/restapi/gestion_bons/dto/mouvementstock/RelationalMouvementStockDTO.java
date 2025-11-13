package com.restapi.gestion_bons.dto.mouvementstock;

import com.restapi.gestion_bons.dto.produit.ProduitResponseDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RelationalMouvementStockDTO extends BaseMouvementStockDTO {
    private ProduitResponseDTO produit;
    private LotRef lot;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LotRef {
        private Long id;
        private String numeroLot;
    }
}

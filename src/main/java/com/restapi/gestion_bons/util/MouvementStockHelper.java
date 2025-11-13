package com.restapi.gestion_bons.util;

import java.util.ArrayList;
import java.util.List;

import com.restapi.gestion_bons.entitie.Lot;
import com.restapi.gestion_bons.entitie.MouvementStock;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;

public class MouvementStockHelper {

    public static List<MouvementStock> creaMouvementStocksFromLots(List<Lot> lots) {
        List<MouvementStock> mvms = new ArrayList<>();

        lots.forEach(l -> {
            MouvementStock m = MouvementStock.builder()
                    .typeMouvement(TypeMouvement.ENTREE)
                    .dateMouvement(l.getDateEntree())
                    .quantite(l.getQuantiteInitiale())
                    .prixUnitaireLot(l.getPrixAchatUnitaire().doubleValue())
                    .produit(l.getProduit())
                    .lot(l)
                    .build();
            mvms.add(m);
        });
        return mvms;
    }
}

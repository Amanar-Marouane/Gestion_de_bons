package com.restapi.gestion_bons.dao;

import com.restapi.gestion_bons.entitie.MouvementStock;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementStockDAO extends JpaRepository<MouvementStock, Long> {
    List<MouvementStock> findAllByOrderByDateMouvementDesc();

    List<MouvementStock> findByProduitIdOrderByDateMouvementDesc(Long produitId);

    List<MouvementStock> findByLotIdOrderByDateMouvementDesc(Long lotId);

    List<MouvementStock> findAllByTypeMouvementOrderByDateMouvementDesc(TypeMouvement typeMouvement);

    List<MouvementStock> findByDateMouvementBetweenOrderByDateMouvementDesc(
            LocalDateTime start,
            LocalDateTime end);
}

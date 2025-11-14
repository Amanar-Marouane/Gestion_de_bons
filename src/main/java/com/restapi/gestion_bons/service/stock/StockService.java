package com.restapi.gestion_bons.service.stock;

import com.restapi.gestion_bons.contracts.StockContract;
import com.restapi.gestion_bons.dao.LotDAO;
import com.restapi.gestion_bons.dao.MouvementStockDAO;
import com.restapi.gestion_bons.dao.ProduitDAO;
import com.restapi.gestion_bons.dto.stock.StockAlertDTO;
import com.restapi.gestion_bons.dto.stock.StockGlobalDTO;
import com.restapi.gestion_bons.dto.stock.StockProduitDetailDTO;
import com.restapi.gestion_bons.dto.stock.StockValorisationDTO;
import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.entitie.Lot;
import com.restapi.gestion_bons.entitie.MouvementStock;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.entitie.enums.LotStatus;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;
import com.restapi.gestion_bons.mapper.MouvementStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService implements StockContract {

        private final LotDAO lotDAO;
        private final ProduitDAO produitDAO;
        private final MouvementStockDAO mouvementStockDAO;
        private final MouvementStockMapper mouvementStockMapper;

        @Override
        @Transactional(readOnly = true)
        public List<StockGlobalDTO> getStockGlobal() {
                List<Produit> produits = produitDAO.findAll();

                return produits.stream().map(produit -> {
                        List<Lot> lots = lotDAO.findByProduitIdAndStatut(produit.getId(), LotStatus.DISPONIBLE);
                        int quantiteTotale = lots.stream()
                                        .mapToInt(Lot::getQuantiteRestante)
                                        .sum();

                        BigDecimal valorisation = lots.stream()
                                        .map(lot -> lot.getPrixAchatUnitaire()
                                                        .multiply(BigDecimal.valueOf(lot.getQuantiteRestante())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                        return StockGlobalDTO.builder()
                                        .produitId(produit.getId())
                                        .produitReference(produit.getReference())
                                        .produitNom(produit.getNom())
                                        .quantiteDisponible(quantiteTotale)
                                        .valorisation(valorisation)
                                        .nombreLots(lots.size())
                                        .build();
                }).collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public StockProduitDetailDTO getStockByProduitId(Long produitId) {
                Produit produit = produitDAO.findById(produitId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Produit non trouvé avec l'id: " + produitId));

                List<Lot> lots = lotDAO.findByProduitIdOrderByDateEntreeAsc(produitId);

                int quantiteTotale = lots.stream()
                                .filter(lot -> lot.getStatut() == LotStatus.DISPONIBLE)
                                .mapToInt(Lot::getQuantiteRestante)
                                .sum();

                BigDecimal valorisationFIFO = calculerValorisationFIFO(lots);

                List<StockProduitDetailDTO.LotInfo> lotsInfo = lots.stream()
                                .map(lot -> StockProduitDetailDTO.LotInfo.builder()
                                                .lotId(lot.getId())
                                                .numeroLot(lot.getNumeroLot())
                                                .dateEntree(lot.getDateEntree())
                                                .quantiteInitiale(lot.getQuantiteInitiale())
                                                .quantiteRestante(lot.getQuantiteRestante())
                                                .prixAchatUnitaire(lot.getPrixAchatUnitaire())
                                                .statut(lot.getStatut())
                                                .build())
                                .collect(Collectors.toList());

                return StockProduitDetailDTO.builder()
                                .produitId(produit.getId())
                                .produitReference(produit.getReference())
                                .produitNom(produit.getNom())
                                .quantiteTotale(quantiteTotale)
                                .valorisationFIFO(valorisationFIFO)
                                .lots(lotsInfo)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public List<MouvementStockResponseDTO> getMouvements() {
                return mouvementStockDAO.findAllByOrderByDateMouvementDesc().stream()
                                .map(mouvementStockMapper::toResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<MouvementStockResponseDTO> getMouvementsByProduitId(Long produitId) {
                if (!produitDAO.existsById(produitId)) {
                        throw new EntityNotFoundException("Produit non trouvé avec l'id: " + produitId);
                }

                return mouvementStockDAO.findByProduitIdOrderByDateMouvementDesc(produitId).stream()
                                .map(mouvementStockMapper::toResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<MouvementStockResponseDTO> getMouvementsByLotId(Long lotId) {
                if (!produitDAO.existsById(lotId)) {
                        throw new EntityNotFoundException("Lot non trouvé avec l'id: " + lotId);
                }

                return mouvementStockDAO.findByLotIdOrderByDateMouvementDesc(lotId).stream()
                                .map(mouvementStockMapper::toResponseDto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<StockAlertDTO> getAlertes() {
                List<Produit> produits = produitDAO.findAll();

                return produits.stream()
                                .filter(produit -> {
                                        int stockActuel = lotDAO
                                                        .findByProduitIdAndStatut(produit.getId(), LotStatus.DISPONIBLE)
                                                        .stream()
                                                        .mapToInt(Lot::getQuantiteRestante)
                                                        .sum();
                                        return stockActuel < produit.getReorderPoint();
                                })
                                .map(produit -> {
                                        int stockActuel = lotDAO
                                                        .findByProduitIdAndStatut(produit.getId(), LotStatus.DISPONIBLE)
                                                        .stream()
                                                        .mapToInt(Lot::getQuantiteRestante)
                                                        .sum();

                                        return StockAlertDTO.builder()
                                                        .produitId(produit.getId())
                                                        .produitReference(produit.getReference())
                                                        .produitNom(produit.getNom())
                                                        .stockActuel(stockActuel)
                                                        .seuilMinimum(produit.getReorderPoint())
                                                        .quantiteAReapprovisionner(
                                                                        produit.getReorderPoint() - stockActuel)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public StockValorisationDTO getValorisation() {
                List<Lot> lotsDisponibles = lotDAO.findByStatut(LotStatus.DISPONIBLE);

                BigDecimal valorisationTotale = lotsDisponibles.stream()
                                .map(lot -> lot.getPrixAchatUnitaire()
                                                .multiply(BigDecimal.valueOf(lot.getQuantiteRestante())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                int nombreProduitsDistincts = (int) lotsDisponibles.stream()
                                .map(lot -> lot.getProduit().getId())
                                .distinct()
                                .count();

                int quantiteTotale = lotsDisponibles.stream()
                                .mapToInt(Lot::getQuantiteRestante)
                                .sum();

                return StockValorisationDTO.builder()
                                .valorisationTotale(valorisationTotale)
                                .nombreProduitsDistincts(nombreProduitsDistincts)
                                .quantiteTotaleArticles(quantiteTotale)
                                .nombreLotsActifs(lotsDisponibles.size())
                                .methodeVlorisation("FIFO")
                                .build();
        }

        private BigDecimal calculerValorisationFIFO(List<Lot> lots) {
                return lots.stream()
                                .filter(lot -> lot.getStatut() == LotStatus.DISPONIBLE)
                                .map(lot -> lot.getPrixAchatUnitaire()
                                                .multiply(BigDecimal.valueOf(lot.getQuantiteRestante())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        @Override
        public List<MouvementStockResponseDTO> getMouvementsByTypeMouvement(TypeMouvement type) {
                List<MouvementStock> stocks = mouvementStockDAO.findAllByTypeMouvementOrderByDateMouvementDesc(type);
                return mouvementStockMapper.toResponseDtoList(stocks);
        }

        @Override
        public List<MouvementStockResponseDTO> getMouvementsByDateInterval(LocalDateTime start, LocalDateTime end) {
                List<MouvementStock> stocks = mouvementStockDAO
                                .findByDateMouvementBetweenOrderByDateMouvementDesc(start, end);
                return mouvementStockMapper.toResponseDtoList(stocks);
        }

}

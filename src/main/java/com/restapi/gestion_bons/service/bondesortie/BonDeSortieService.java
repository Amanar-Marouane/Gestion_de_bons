package com.restapi.gestion_bons.service.bondesortie;

import com.restapi.gestion_bons.contracts.BonDeSortieContract;
import com.restapi.gestion_bons.dao.*;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieCreateDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieResponseDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieUpdateDTO;
import com.restapi.gestion_bons.entitie.*;
import com.restapi.gestion_bons.entitie.enums.BonDeSortieStatus;
import com.restapi.gestion_bons.entitie.enums.LotStatus;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;
import com.restapi.gestion_bons.mapper.BonDeSortieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BonDeSortieService implements BonDeSortieContract {

    private final BonDeSortieDAO bonDeSortieDAO;
    private final BonDeSortieLigneDAO bonDeSortieLigneDAO;
    private final AtelierDAO atelierDAO;
    private final ProduitDAO produitDAO;
    private final LotDAO lotDAO;
    private final MouvementStockDAO mouvementStockDAO;
    private final BonDeSortieMapper bonDeSortieMapper;

    @Override
    public BonDeSortieResponseDTO save(BonDeSortieCreateDTO dto) {
        Atelier atelier = atelierDAO.findById(dto.getAtelierId())
                .orElseThrow(() -> new EntityNotFoundException("Atelier non trouvé avec l'id: " + dto.getAtelierId()));

        BonDeSortie bonDeSortie = BonDeSortie.builder()
                .numeroBon(genererNumeroBon())
                .dateSortie(dto.getDateSortie())
                .motifSortie(dto.getMotifSortie())
                .statut(BonDeSortieStatus.BROUILLON)
                .atelier(atelier)
                .build();

        // Créer les lignes du bon
        List<BonDeSortieLigne> lignes = dto.getLignes().stream()
                .map(ligneDTO -> {
                    Produit produit = produitDAO.findById(ligneDTO.getProduitId())
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Produit non trouvé avec l'id: " + ligneDTO.getProduitId()));

                    return BonDeSortieLigne.builder()
                            .bonDeSortie(bonDeSortie)
                            .produit(produit)
                            .quantiteDemandee(ligneDTO.getQuantiteDemandee())
                            .build();
                })
                .collect(Collectors.toList());

        bonDeSortie.setBonDeSortieLignes(lignes);

        BonDeSortie saved = bonDeSortieDAO.save(bonDeSortie);
        saved.getBonDeSortieLignes().forEach(ligne -> {
            ligne.setBonDeSortie(saved);
            bonDeSortieLigneDAO.save(ligne);
        });
        return bonDeSortieMapper.toResponseDto(saved);
    }

    @Override
    public BonDeSortieResponseDTO update(Long id, BonDeSortieUpdateDTO dto) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id: " + id));

        if (bonDeSortie.getStatut() != BonDeSortieStatus.BROUILLON) {
            throw new IllegalStateException("Seuls les bons de sortie brouillons peuvent être modifiés");
        }

        // Mise à jour
        bonDeSortie.setDateSortie(dto.getDateSortie());
        bonDeSortie.setMotifSortie(dto.getMotifSortie());

        if (dto.getAtelierId() != null) {
            Atelier atelier = atelierDAO.findById(dto.getAtelierId())
                    .orElseThrow(
                            () -> new EntityNotFoundException("Atelier non trouvé avec l'id: " + dto.getAtelierId()));
            bonDeSortie.setAtelier(atelier);
        }

        BonDeSortie updated = bonDeSortieDAO.save(bonDeSortie);
        return bonDeSortieMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BonDeSortieResponseDTO findById(Long id) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id: " + id));
        return bonDeSortieMapper.toResponseDto(bonDeSortie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonDeSortieResponseDTO> findAll() {
        return bonDeSortieDAO.findAll().stream()
                .map(bonDeSortieMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id: " + id));

        if (bonDeSortie.getStatut() == BonDeSortieStatus.VALIDE) {
            throw new IllegalStateException("Un bon de sortie validé ne peut pas être supprimé");
        }

        bonDeSortieDAO.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BonDeSortieResponseDTO findByNumeroBon(String numeroBon) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findByNumeroBon(numeroBon)
                .orElseThrow(
                        () -> new EntityNotFoundException("Bon de sortie non trouvé avec le numéro: " + numeroBon));
        return bonDeSortieMapper.toResponseDto(bonDeSortie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BonDeSortieResponseDTO> findByAtelierId(Long atelierId) {
        if (!atelierDAO.existsById(atelierId)) {
            throw new EntityNotFoundException("Atelier non trouvé avec l'id: " + atelierId);
        }

        return bonDeSortieDAO.findByAtelierId(atelierId).stream()
                .map(bonDeSortieMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BonDeSortieResponseDTO valider(Long id) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id: " + id));

        if (bonDeSortie.getStatut() != BonDeSortieStatus.BROUILLON) {
            throw new IllegalStateException("Seuls les bons de sortie brouillons peuvent être validés");
        }

        // Traiter chaque ligne avec FIFO
        for (BonDeSortieLigne ligne : bonDeSortie.getBonDeSortieLignes()) {
            traiterSortieFIFO(ligne, bonDeSortie);
        }

        bonDeSortie.setStatut(BonDeSortieStatus.VALIDE);
        BonDeSortie validated = bonDeSortieDAO.save(bonDeSortie);

        return bonDeSortieMapper.toResponseDto(validated);
    }

    @Override
    public BonDeSortieResponseDTO annuler(Long id) {
        BonDeSortie bonDeSortie = bonDeSortieDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bon de sortie non trouvé avec l'id: " + id));

        if (bonDeSortie.getStatut() == BonDeSortieStatus.VALIDE) {
            throw new IllegalStateException("Un bon de sortie validé ne peut pas être annulé");
        }

        bonDeSortie.setStatut(BonDeSortieStatus.ANNULE);
        BonDeSortie cancelled = bonDeSortieDAO.save(bonDeSortie);

        return bonDeSortieMapper.toResponseDto(cancelled);
    }

    private void traiterSortieFIFO(BonDeSortieLigne ligne, BonDeSortie bonDeSortie) {
        Produit produit = ligne.getProduit();
        int quantiteRestante = ligne.getQuantiteDemandee();

        // Récupérer les lots FIFO (plus anciens en premier)
        List<Lot> lots = lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(
                produit.getId(), LotStatus.DISPONIBLE);

        if (lots.isEmpty()) {
            throw new IllegalStateException("Aucun lot disponible pour le produit: " + produit.getNom());
        }

        for (Lot lot : lots) {
            if (quantiteRestante <= 0)
                break;

            int quantiteAPrever = Math.min(quantiteRestante, lot.getQuantiteRestante());

            // Créer le mouvement de stock
            MouvementStock mouvement = MouvementStock.builder()
                    .typeMouvement(TypeMouvement.SORTIE)
                    .dateMouvement(LocalDateTime.now())
                    .quantite(quantiteAPrever)
                    .prixUnitaireLot(lot.getPrixAchatUnitaire().doubleValue())
                    .produit(produit)
                    .lot(lot)
                    .bonDeSortie(bonDeSortie)
                    .build();

            mouvementStockDAO.save(mouvement);

            // Mettre à jour le lot
            lot.setQuantiteRestante(lot.getQuantiteRestante() - quantiteAPrever);
            if (lot.getQuantiteRestante() == 0) {
                lot.setStatut(LotStatus.EPUISE);
            }
            lotDAO.save(lot);

            quantiteRestante -= quantiteAPrever;
        }

        if (quantiteRestante > 0) {
            throw new IllegalStateException(
                    "Stock insuffisant pour le produit: " + produit.getNom() +
                            ". Manque: " + quantiteRestante + " unités");
        }
    }

    private String genererNumeroBon() {
        String prefix = "BS-";
        long count = bonDeSortieDAO.count() + 1;
        return prefix + String.format("%06d", count);
    }
}

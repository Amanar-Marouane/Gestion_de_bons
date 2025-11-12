package com.restapi.gestion_bons;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restapi.gestion_bons.dao.*;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieResponseDTO;
import com.restapi.gestion_bons.entitie.*;
import com.restapi.gestion_bons.entitie.enums.BonDeSortieStatus;
import com.restapi.gestion_bons.entitie.enums.LotStatus;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;
import com.restapi.gestion_bons.mapper.BonDeSortieMapper;
import com.restapi.gestion_bons.service.bondesortie.BonDeSortieService;

@ExtendWith(MockitoExtension.class)
public class BonDeSortieValidationTest {

    @Mock
    private BonDeSortieDAO bonDeSortieDAO;

    @Mock
    private BonDeSortieLigneDAO bonDeSortieLigneDAO;

    @Mock
    private AtelierDAO atelierDAO;

    @Mock
    private ProduitDAO produitDAO;

    @Mock
    private LotDAO lotDAO;

    @Mock
    private MouvementStockDAO mouvementStockDAO;

    @Mock
    private BonDeSortieMapper bonDeSortieMapper;

    @InjectMocks
    private BonDeSortieService bonDeSortieService;

    private BonDeSortie bonBrouillon;
    private Produit produit;
    private Lot lot;
    private BonDeSortieLigne ligne;
    private Atelier atelier;

    @BeforeEach
    void setUp() {
        atelier = Atelier.builder()
                .id(1L)
                .nom("Atelier Test")
                .build();

        produit = Produit.builder()
                .id(1L)
                .nom("Produit Test")
                .reference("REF001")
                .build();

        lot = Lot.builder()
                .id(1L)
                .numeroLot("LOT-2024-001")
                .dateEntree(LocalDateTime.now().minusDays(5))
                .produit(produit)
                .quantiteInitiale(100)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        ligne = BonDeSortieLigne.builder()
                .id(1L)
                .produit(produit)
                .quantiteDemandee(50)
                .build();

        bonBrouillon = BonDeSortie.builder()
                .id(1L)
                .numeroBon("BS-000001")
                .dateSortie(new Date())
                .motifSortie("Test de validation")
                .statut(BonDeSortieStatus.BROUILLON)
                .atelier(atelier)
                .bonDeSortieLignes(new ArrayList<>(Arrays.asList(ligne)))
                .build();

        ligne.setBonDeSortie(bonBrouillon);
    }

    @Test
    void testValidationBonDeSortie_TransitionBrouillonVersValide() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        BonDeSortieResponseDTO result = bonDeSortieService.valider(1L);

        assertNotNull(result);
        ArgumentCaptor<BonDeSortie> bonCaptor = ArgumentCaptor.forClass(BonDeSortie.class);
        verify(bonDeSortieDAO).save(bonCaptor.capture());

        BonDeSortie bonSauvegarde = bonCaptor.getValue();
        assertEquals(BonDeSortieStatus.VALIDE, bonSauvegarde.getStatut(),
                "Le statut doit passer de BROUILLON à VALIDE");
    }

    @Test
    void testValidation_CreeMouvementStockAutomatiquement() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        ArgumentCaptor<MouvementStock> mouvementCaptor = ArgumentCaptor.forClass(MouvementStock.class);
        verify(mouvementStockDAO, times(1)).save(mouvementCaptor.capture());

        MouvementStock mouvement = mouvementCaptor.getValue();
        assertNotNull(mouvement, "Un mouvement de stock doit être créé");
        assertEquals(TypeMouvement.SORTIE, mouvement.getTypeMouvement());
        assertEquals(50, mouvement.getQuantite());
        assertEquals(produit.getId(), mouvement.getProduit().getId());
        assertEquals(lot.getId(), mouvement.getLot().getId());
        assertEquals(bonBrouillon.getId(), mouvement.getBonDeSortie().getId());
    }

    @Test
    void testValidation_MiseAJourQuantitesLot() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        ArgumentCaptor<Lot> lotCaptor = ArgumentCaptor.forClass(Lot.class);
        verify(lotDAO, times(1)).save(lotCaptor.capture());

        Lot lotMisAJour = lotCaptor.getValue();
        assertEquals(50, lotMisAJour.getQuantiteRestante(),
                "La quantité restante doit être réduite de 50 (100 - 50)");
        assertEquals(LotStatus.DISPONIBLE, lotMisAJour.getStatut(),
                "Le lot doit rester DISPONIBLE car il reste du stock");
    }

    @Test
    void testValidation_LotEpuiseQuandQuantiteZero() {
        ligne.setQuantiteDemandee(100); // Épuiser complètement le lot
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        ArgumentCaptor<Lot> lotCaptor = ArgumentCaptor.forClass(Lot.class);
        verify(lotDAO).save(lotCaptor.capture());

        Lot lotMisAJour = lotCaptor.getValue();
        assertEquals(0, lotMisAJour.getQuantiteRestante());
        assertEquals(LotStatus.EPUISE, lotMisAJour.getStatut(),
                "Le lot doit passer à EPUISE quand la quantité atteint zéro");
    }

    @Test
    void testValidation_EnregistreDateMouvement() {
        LocalDateTime avant = LocalDateTime.now().minusSeconds(1);
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);
        LocalDateTime apres = LocalDateTime.now().plusSeconds(1);

        ArgumentCaptor<MouvementStock> mouvementCaptor = ArgumentCaptor.forClass(MouvementStock.class);
        verify(mouvementStockDAO).save(mouvementCaptor.capture());

        MouvementStock mouvement = mouvementCaptor.getValue();
        assertNotNull(mouvement.getDateMouvement(), "La date du mouvement doit être enregistrée");
        assertTrue(mouvement.getDateMouvement().isAfter(avant) && mouvement.getDateMouvement().isBefore(apres),
                "La date du mouvement doit être proche de l'heure actuelle");
    }

    @Test
    void testValidation_EnregistrePrixUnitaireDuLot() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        ArgumentCaptor<MouvementStock> mouvementCaptor = ArgumentCaptor.forClass(MouvementStock.class);
        verify(mouvementStockDAO).save(mouvementCaptor.capture());

        MouvementStock mouvement = mouvementCaptor.getValue();
        assertEquals(10.00, mouvement.getPrixUnitaireLot(),
                "Le prix unitaire du lot doit être enregistré dans le mouvement");
    }

    @Test
    void testValidation_EchecSiBonDejaValide() {
        bonBrouillon.setStatut(BonDeSortieStatus.VALIDE);
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bonDeSortieService.valider(1L));

        assertEquals("Seuls les bons de sortie brouillons peuvent être validés", exception.getMessage());
        verify(mouvementStockDAO, never()).save(any(MouvementStock.class));
        verify(lotDAO, never()).save(any(Lot.class));
    }

    @Test
    void testValidation_EchecSiBonAnnule() {
        bonBrouillon.setStatut(BonDeSortieStatus.ANNULE);
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bonDeSortieService.valider(1L));

        assertEquals("Seuls les bons de sortie brouillons peuvent être validés", exception.getMessage());
        verify(mouvementStockDAO, never()).save(any(MouvementStock.class));
    }

    @Test
    void testValidation_AvecPlusieursLignes() {
        Produit produit2 = Produit.builder()
                .id(2L)
                .nom("Produit 2")
                .reference("REF002")
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .numeroLot("LOT-2024-002")
                .dateEntree(LocalDateTime.now().minusDays(3))
                .produit(produit2)
                .quantiteRestante(80)
                .prixAchatUnitaire(new BigDecimal("15.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        BonDeSortieLigne ligne2 = BonDeSortieLigne.builder()
                .id(2L)
                .produit(produit2)
                .quantiteDemandee(30)
                .bonDeSortie(bonBrouillon)
                .build();

        bonBrouillon.getBonDeSortieLignes().add(ligne2);

        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit2.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot2));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        verify(mouvementStockDAO, times(2)).save(any(MouvementStock.class));
        verify(lotDAO, times(2)).save(any(Lot.class));
    }

    @Test
    void testValidation_UtilisePlusieursLotsSiNecessaire() {
        Lot lot2 = Lot.builder()
                .id(2L)
                .numeroLot("LOT-2024-002")
                .dateEntree(LocalDateTime.now().minusDays(2))
                .produit(produit)
                .quantiteRestante(80)
                .prixAchatUnitaire(new BigDecimal("12.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        ligne.setQuantiteDemandee(150); // Nécessite 2 lots

        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot, lot2)); // FIFO: lot1 puis lot2
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        verify(mouvementStockDAO, times(2)).save(any(MouvementStock.class));
        verify(lotDAO, times(2)).save(any(Lot.class));

        // Vérifier que le premier lot est épuisé
        ArgumentCaptor<Lot> lotCaptor = ArgumentCaptor.forClass(Lot.class);
        verify(lotDAO, times(2)).save(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getAllValues();
        assertEquals(0, lotsSauvegardes.get(0).getQuantiteRestante());
        assertEquals(LotStatus.EPUISE, lotsSauvegardes.get(0).getStatut());
        assertEquals(30, lotsSauvegardes.get(1).getQuantiteRestante()); // 80 - 50
    }

    @Test
    void testValidation_EchecSiStockInsuffisant() {
        ligne.setQuantiteDemandee(150); // Plus que disponible
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot)); // Seulement 100 disponibles

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bonDeSortieService.valider(1L));

        assertTrue(exception.getMessage().contains("Stock insuffisant"));
        assertTrue(exception.getMessage().contains("Produit Test"));
        assertTrue(exception.getMessage().contains("50")); // Manque 50 unités
        verify(bonDeSortieDAO, never()).save(any(BonDeSortie.class));
    }

    @Test
    void testValidation_EchecSiAucunLotDisponible() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList()); // Aucun lot

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bonDeSortieService.valider(1L));

        assertEquals("Aucun lot disponible pour le produit: Produit Test", exception.getMessage());
        verify(mouvementStockDAO, never()).save(any(MouvementStock.class));
    }

    @Test
    void testValidation_LienEntreMouvementEtBonDeSortie() {
        when(bonDeSortieDAO.findById(1L)).thenReturn(Optional.of(bonBrouillon));
        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produit.getId(), LotStatus.DISPONIBLE))
                .thenReturn(Arrays.asList(lot));
        when(bonDeSortieDAO.save(any(BonDeSortie.class))).thenReturn(bonBrouillon);
        when(bonDeSortieMapper.toResponseDto(any(BonDeSortie.class)))
                .thenReturn(new BonDeSortieResponseDTO());

        bonDeSortieService.valider(1L);

        ArgumentCaptor<MouvementStock> mouvementCaptor = ArgumentCaptor.forClass(MouvementStock.class);
        verify(mouvementStockDAO).save(mouvementCaptor.capture());

        MouvementStock mouvement = mouvementCaptor.getValue();
        assertNotNull(mouvement.getBonDeSortie(),
                "Le mouvement doit être lié au bon de sortie");
        assertEquals(bonBrouillon.getId(), mouvement.getBonDeSortie().getId(),
                "Le mouvement doit référencer le bon de sortie correct");
    }
}

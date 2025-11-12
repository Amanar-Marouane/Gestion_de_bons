package com.restapi.gestion_bons;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.restapi.gestion_bons.dao.LotDAO;
import com.restapi.gestion_bons.dao.MouvementStockDAO;
import com.restapi.gestion_bons.dao.ProduitDAO;
import com.restapi.gestion_bons.dto.stock.StockProduitDetailDTO;
import com.restapi.gestion_bons.dto.stock.StockValorisationDTO;
import com.restapi.gestion_bons.entitie.Lot;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.entitie.enums.LotStatus;
import com.restapi.gestion_bons.mapper.MouvementStockMapper;
import com.restapi.gestion_bons.service.stock.StockService;

@ExtendWith(MockitoExtension.class)
public class StockValorisationTest {

    @Mock
    private LotDAO lotDAO;

    @Mock
    private ProduitDAO produitDAO;

    @Mock
    private MouvementStockDAO mouvementStockDAO;

    @Mock
    private MouvementStockMapper mouvementStockMapper;

    @InjectMocks
    private StockService stockService;

    private Produit produit;
    private List<Lot> lotsMultiplesPrix;

    @BeforeEach
    void setUp() {
        produit = Produit.builder()
                .id(1L)
                .nom("Produit Test")
                .reference("REF001")
                .reorderPoint(50)
                .build();

        // Créer plusieurs lots à prix différents (ordre chronologique)
        Lot lot1 = Lot.builder()
                .id(1L)
                .numeroLot("LOT-2024-001")
                .dateEntree(LocalDateTime.now().minusDays(10))
                .produit(produit)
                .quantiteInitiale(100)
                .quantiteRestante(80)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .numeroLot("LOT-2024-002")
                .dateEntree(LocalDateTime.now().minusDays(5))
                .produit(produit)
                .quantiteInitiale(150)
                .quantiteRestante(120)
                .prixAchatUnitaire(new BigDecimal("12.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot3 = Lot.builder()
                .id(3L)
                .numeroLot("LOT-2024-003")
                .dateEntree(LocalDateTime.now().minusDays(1))
                .produit(produit)
                .quantiteInitiale(200)
                .quantiteRestante(200)
                .prixAchatUnitaire(new BigDecimal("15.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        lotsMultiplesPrix = Arrays.asList(lot1, lot2, lot3);
    }

    @Test
    void testValorisationTotaleStock_AvecPlusieursLots() {
        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(lotsMultiplesPrix);

        StockValorisationDTO result = stockService.getValorisation();

        // Calcul attendu: (80 * 10.00) + (120 * 12.00) + (200 * 15.00) = 800 + 1440 +
        // 3000 = 5240
        BigDecimal valorisationAttendue = new BigDecimal("5240.00");
        assertNotNull(result);
        assertEquals(valorisationAttendue, result.getValorisationTotale());
        assertEquals(400, result.getQuantiteTotaleArticles()); // 80 + 120 + 200
        assertEquals(3, result.getNombreLotsActifs());
    }

    @Test
    void testValorisationFIFO_ProduitDetail() {
        when(produitDAO.findById(1L)).thenReturn(java.util.Optional.of(produit));
        when(lotDAO.findByProduitIdOrderByDateEntreeAsc(1L)).thenReturn(lotsMultiplesPrix);

        StockProduitDetailDTO result = stockService.getStockByProduitId(1L);

        BigDecimal valorisationAttendue = new BigDecimal("5240.00");
        assertNotNull(result);
        assertEquals(valorisationAttendue, result.getValorisationFIFO());
        assertEquals(400, result.getQuantiteTotale());
        assertEquals("FIFO", stockService.getValorisation().getMethodeVlorisation());
    }

    @Test
    void testValorisationFIFO_OrdreLotsPrisEnCompte() {
        when(produitDAO.findById(1L)).thenReturn(java.util.Optional.of(produit));
        when(lotDAO.findByProduitIdOrderByDateEntreeAsc(1L)).thenReturn(lotsMultiplesPrix);

        StockProduitDetailDTO result = stockService.getStockByProduitId(1L);

        List<StockProduitDetailDTO.LotInfo> lots = result.getLots();
        assertEquals(3, lots.size());

        // Vérifier que le lot le plus ancien est en premier
        assertEquals("LOT-2024-001", lots.get(0).getNumeroLot());
        assertEquals(new BigDecimal("10.00"), lots.get(0).getPrixAchatUnitaire());

        // Vérifier que le lot le plus récent est en dernier
        assertEquals("LOT-2024-003", lots.get(2).getNumeroLot());
        assertEquals(new BigDecimal("15.00"), lots.get(2).getPrixAchatUnitaire());
    }

    @Test
    void testValorisationAvecLotsDifferentsPrix() {
        // 3 lots avec des prix très différents
        Lot lotBas = Lot.builder()
                .id(1L)
                .numeroLot("LOT-BAS")
                .dateEntree(LocalDateTime.now().minusDays(3))
                .produit(produit)
                .quantiteRestante(50)
                .prixAchatUnitaire(new BigDecimal("5.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lotMoyen = Lot.builder()
                .id(2L)
                .numeroLot("LOT-MOYEN")
                .dateEntree(LocalDateTime.now().minusDays(2))
                .produit(produit)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("20.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lotHaut = Lot.builder()
                .id(3L)
                .numeroLot("LOT-HAUT")
                .dateEntree(LocalDateTime.now().minusDays(1))
                .produit(produit)
                .quantiteRestante(75)
                .prixAchatUnitaire(new BigDecimal("50.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        List<Lot> lots = Arrays.asList(lotBas, lotMoyen, lotHaut);
        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(lots);

        StockValorisationDTO result = stockService.getValorisation();

        // Calcul: (50 * 5) + (100 * 20) + (75 * 50) = 250 + 2000 + 3750 = 6000
        assertEquals(new BigDecimal("6000.00"), result.getValorisationTotale());
    }

    @Test
    void testValorisationStockVide() {
        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(Collections.emptyList());

        StockValorisationDTO result = stockService.getValorisation();

        assertEquals(BigDecimal.ZERO, result.getValorisationTotale());
        assertEquals(0, result.getQuantiteTotaleArticles());
        assertEquals(0, result.getNombreLotsActifs());
        assertEquals(0, result.getNombreProduitsDistincts());
    }

    @Test
    void testValorisationUnSeulLot() {
        Lot lotUnique = Lot.builder()
                .id(1L)
                .numeroLot("LOT-UNIQUE")
                .dateEntree(LocalDateTime.now())
                .produit(produit)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("25.50"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(Collections.singletonList(lotUnique));

        StockValorisationDTO result = stockService.getValorisation();

        // Calcul: 100 * 25.50 = 2550
        assertEquals(new BigDecimal("2550.00"), result.getValorisationTotale());
        assertEquals(100, result.getQuantiteTotaleArticles());
        assertEquals(1, result.getNombreLotsActifs());
    }

    @Test
    void testValorisationProduitDetail_AvecLotsMixtes() {
        Lot lotDisponible1 = Lot.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .dateEntree(LocalDateTime.now().minusDays(5))
                .produit(produit)
                .quantiteInitiale(100)
                .quantiteRestante(80)
                .prixAchatUnitaire(new BigDecimal("12.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lotDisponible2 = Lot.builder()
                .id(2L)
                .numeroLot("LOT-002")
                .dateEntree(LocalDateTime.now().minusDays(2))
                .produit(produit)
                .quantiteInitiale(150)
                .quantiteRestante(150)
                .prixAchatUnitaire(new BigDecimal("14.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lotEpuise = Lot.builder()
                .id(3L)
                .numeroLot("LOT-003")
                .dateEntree(LocalDateTime.now().minusDays(8))
                .produit(produit)
                .quantiteInitiale(200)
                .quantiteRestante(0)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .statut(LotStatus.EPUISE)
                .build();

        List<Lot> lots = Arrays.asList(lotEpuise, lotDisponible1, lotDisponible2);
        when(produitDAO.findById(1L)).thenReturn(java.util.Optional.of(produit));
        when(lotDAO.findByProduitIdOrderByDateEntreeAsc(1L)).thenReturn(lots);

        StockProduitDetailDTO result = stockService.getStockByProduitId(1L);

        // Valorisation: (80 * 12.00) + (150 * 14.00) = 960 + 2100 = 3060
        assertEquals(new BigDecimal("3060.00"), result.getValorisationFIFO());
        assertEquals(230, result.getQuantiteTotale());
        assertEquals(3, result.getLots().size());
    }

    @Test
    void testMethodeValorisationEstFIFO() {
        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(lotsMultiplesPrix);

        StockValorisationDTO result = stockService.getValorisation();

        assertEquals("FIFO", result.getMethodeVlorisation());
    }

    @Test
    void testValorisationAvecPrixDecimaux() {
        Lot lot1 = Lot.builder()
                .id(1L)
                .numeroLot("LOT-001")
                .dateEntree(LocalDateTime.now())
                .produit(produit)
                .quantiteRestante(33)
                .prixAchatUnitaire(new BigDecimal("12.567"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .numeroLot("LOT-002")
                .dateEntree(LocalDateTime.now().minusDays(1))
                .produit(produit)
                .quantiteRestante(17)
                .prixAchatUnitaire(new BigDecimal("8.333"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(Arrays.asList(lot2, lot1));

        StockValorisationDTO result = stockService.getValorisation();

        // Calcul: (33 * 12.567) + (17 * 8.333) = 414.711 + 141.661 = 556.372
        BigDecimal valorisationAttendue = new BigDecimal("556.372");
        assertEquals(0, valorisationAttendue.compareTo(result.getValorisationTotale()));
    }

    @Test
    void testNombreProduitsDistinctsValorisés() {
        Produit produit2 = Produit.builder()
                .id(2L)
                .nom("Produit 2")
                .reference("REF002")
                .build();

        Lot lot1 = Lot.builder()
                .id(1L)
                .produit(produit)
                .quantiteRestante(50)
                .prixAchatUnitaire(new BigDecimal("10.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .produit(produit)
                .quantiteRestante(30)
                .prixAchatUnitaire(new BigDecimal("12.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot3 = Lot.builder()
                .id(3L)
                .produit(produit2)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("15.00"))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByStatut(LotStatus.DISPONIBLE)).thenReturn(Arrays.asList(lot1, lot2, lot3));

        StockValorisationDTO result = stockService.getValorisation();

        assertEquals(2, result.getNombreProduitsDistincts());
        assertEquals(3, result.getNombreLotsActifs());
    }
}

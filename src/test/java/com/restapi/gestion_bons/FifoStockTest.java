package com.restapi.gestion_bons;

import com.restapi.gestion_bons.dao.LotDAO;
import com.restapi.gestion_bons.dao.MouvementStockDAO;
import com.restapi.gestion_bons.entitie.BonDeSortie;
import com.restapi.gestion_bons.entitie.BonDeSortieLigne;
import com.restapi.gestion_bons.entitie.Lot;
import com.restapi.gestion_bons.entitie.MouvementStock;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.entitie.enums.LotStatus;
import com.restapi.gestion_bons.service.bondesortie.BonDeSortieService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class FifoStockTest {
    @Mock
    private LotDAO lotDAO;

    @Mock
    private MouvementStockDAO mouvementStockDAO;

    @InjectMocks
    private BonDeSortieService bonDeSortieService;

    private static Produit produitMock;
    private static BonDeSortie bonMock;

    @BeforeAll
    public static void setUp() throws Exception {
        produitMock = Produit.builder().id(1L).nom("Produit Test").build();
        bonMock = BonDeSortie.builder().id(1L).numeroBon("BS-000001").build();
    }

    @Test
    public void consumeSingleLotWithSuccess()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        Lot lot1 = Lot.builder()
                .id(1L)
                .produit(produitMock)
                .quantiteRestante(5)
                .prixAchatUnitaire(BigDecimal.valueOf(10))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produitMock.getId(), LotStatus.DISPONIBLE))
                .thenReturn(List.of(lot1));

        BonDeSortieLigne ligne = BonDeSortieLigne.builder()
                .produit(produitMock)
                .quantiteDemandee(5)
                .build();

        var method = BonDeSortieService.class.getDeclaredMethod("traiterSortieFIFO",
                BonDeSortieLigne.class, BonDeSortie.class);
        method.setAccessible(true);
        method.invoke(bonDeSortieService, ligne, bonMock);

        verify(lotDAO, times(1)).save(any(Lot.class));
        verify(mouvementStockDAO, times(1)).save(any(MouvementStock.class));

        assertEquals(0, lot1.getQuantiteRestante());
        assertEquals(LotStatus.EPUISE, lot1.getStatut());
    }

    @Test
    public void consumeMultiLotsWithSuccessButNotAllQuantity()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        Lot lot1 = Lot.builder()
                .id(1L)
                .produit(produitMock)
                .quantiteRestante(5)
                .prixAchatUnitaire(BigDecimal.valueOf(10))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .produit(produitMock)
                .quantiteRestante(8)
                .prixAchatUnitaire(BigDecimal.valueOf(11))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produitMock.getId(), LotStatus.DISPONIBLE))
                .thenReturn(List.of(lot1, lot2));

        BonDeSortieLigne ligne = BonDeSortieLigne.builder()
                .produit(produitMock)
                .quantiteDemandee(12)
                .build();

        var method = BonDeSortieService.class.getDeclaredMethod("traiterSortieFIFO",
                BonDeSortieLigne.class, BonDeSortie.class);
        method.setAccessible(true);
        method.invoke(bonDeSortieService, ligne, bonMock);

        verify(lotDAO, times(2)).save(any(Lot.class));
        verify(mouvementStockDAO, times(2)).save(any(MouvementStock.class));

        assertEquals(0, lot1.getQuantiteRestante());
        assertEquals(1, lot2.getQuantiteRestante());
        assertEquals(LotStatus.EPUISE, lot1.getStatut());
        assertEquals(LotStatus.DISPONIBLE, lot2.getStatut());
    }

    @Test
    public void insuffisantStockWithException()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        Lot lot1 = Lot.builder()
                .id(1L)
                .produit(produitMock)
                .quantiteRestante(5)
                .prixAchatUnitaire(BigDecimal.valueOf(10))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produitMock.getId(), LotStatus.DISPONIBLE))
                .thenReturn(List.of(lot1));

        BonDeSortieLigne ligne = BonDeSortieLigne.builder()
                .produit(produitMock)
                .quantiteDemandee(10)
                .build();

        var method = BonDeSortieService.class.getDeclaredMethod("traiterSortieFIFO",
                BonDeSortieLigne.class, BonDeSortie.class);
        method.setAccessible(true);

        InvocationTargetException exceptionToCheck = assertThrows(InvocationTargetException.class,
                () -> method.invoke(bonDeSortieService, ligne, bonMock));
        Throwable cause = exceptionToCheck.getCause();

        assertTrue(cause instanceof IllegalStateException);
        assertTrue(cause.getMessage().contains("Stock insuffisant pour le produit:"));
    }

    @Test
    public void consumeMultiLotsWithSuccessButAllQuantity()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        Lot lot1 = Lot.builder()
                .id(1L)
                .produit(produitMock)
                .quantiteRestante(5)
                .prixAchatUnitaire(BigDecimal.valueOf(10))
                .statut(LotStatus.DISPONIBLE)
                .build();

        Lot lot2 = Lot.builder()
                .id(2L)
                .produit(produitMock)
                .quantiteRestante(8)
                .prixAchatUnitaire(BigDecimal.valueOf(11))
                .statut(LotStatus.DISPONIBLE)
                .build();

        when(lotDAO.findByProduitIdAndStatutOrderByDateEntreeAsc(produitMock.getId(), LotStatus.DISPONIBLE))
                .thenReturn(List.of(lot1, lot2));

        BonDeSortieLigne ligne = BonDeSortieLigne.builder()
                .produit(produitMock)
                .quantiteDemandee(13)
                .build();

        var method = BonDeSortieService.class.getDeclaredMethod("traiterSortieFIFO",
                BonDeSortieLigne.class, BonDeSortie.class);
        method.setAccessible(true);
        method.invoke(bonDeSortieService, ligne, bonMock);

        verify(lotDAO, times(2)).save(any(Lot.class));
        verify(mouvementStockDAO, times(2)).save(any(MouvementStock.class));

        assertEquals(0, lot1.getQuantiteRestante());
        assertEquals(0, lot2.getQuantiteRestante());
        assertEquals(LotStatus.EPUISE, lot1.getStatut());
        assertEquals(LotStatus.EPUISE, lot2.getStatut());
    }
}

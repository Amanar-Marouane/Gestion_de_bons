package com.restapi.gestion_bons;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.restapi.gestion_bons.dao.CommandeFournisseurDAO;
import com.restapi.gestion_bons.dao.FournisseurDAO;
import com.restapi.gestion_bons.dao.LigneCommandeDAO;
import com.restapi.gestion_bons.dao.LotDAO;
import com.restapi.gestion_bons.dao.MouvementStockDAO;
import com.restapi.gestion_bons.dao.ProduitDAO;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurResponseDTO;
import com.restapi.gestion_bons.entitie.CommandeFournisseur;
import com.restapi.gestion_bons.entitie.Fournisseur;
import com.restapi.gestion_bons.entitie.LigneCommande;
import com.restapi.gestion_bons.entitie.Lot;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.entitie.enums.CommandeStatus;
import com.restapi.gestion_bons.mapper.CommandeFournisseurMapper;
import com.restapi.gestion_bons.mapper.LigneCommandeMapper;
import com.restapi.gestion_bons.service.commandeFournisseur.CommandeFournisseurService;
import com.restapi.gestion_bons.service.fournisseur.FournisseurService;
import com.restapi.gestion_bons.util.LotHelper;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class StockLotTest {
    @Mock
    private LotDAO lotDAO;

    @Mock
    private ProduitDAO produitDAO;

    @Mock
    private LigneCommandeDAO ligneCommandeDAO;

    @Mock
    private FournisseurDAO fournisseurDAO;

    @Mock
    private CommandeFournisseurDAO commandeFournisseurDAO;

    @Mock
    private MouvementStockDAO mouvementStockDAO;

    @Mock
    private CommandeFournisseurMapper commandeFournisseurMapper;

    @Mock
    private FournisseurService fournisseurService;

    @Mock
    private LotHelper lotHelper;

    @Mock
    private LigneCommandeMapper ligneCommandeMapper;

    @InjectMocks
    private CommandeFournisseurService commandeFournisseurService;

    private CommandeFournisseur commandeValidee;
    private Fournisseur fournisseur;
    private Produit produit;
    private LigneCommande ligneCommande;
    private List<Lot> lotsGeneres;

    @BeforeEach
    void setUp() {
        fournisseur = Fournisseur.builder()
                .id(1L)
                .build();

        produit = Produit.builder()
                .id(1L)
                .nom("Produit Test").reference("REF001")
                .build();

        ligneCommande = LigneCommande.builder()
                .id(1L)
                .produit(produit)
                .quantiteCommandee(100)
                .prixAchatUnitaire(15.50)
                .build();

        commandeValidee = CommandeFournisseur.builder()
                .id(1L)
                .fournisseur(fournisseur)
                .statut(CommandeStatus.VALIDEE)
                .dateCommande(new Date())
                .lignesCommande(Arrays.asList(ligneCommande))
                .build();
        ligneCommande.setCommande(commandeValidee);

        Lot lot = Lot.builder()
                .id(1L)
                .numeroLot("LOT-2024-001")
                .dateEntree(LocalDateTime.now())
                .produit(produit)
                .quantiteInitiale(100)
                .quantiteRestante(100)
                .prixAchatUnitaire(new BigDecimal("15.50"))
                .commandeFournisseur(commandeValidee)
                .build();

        lotsGeneres = Arrays.asList(lot);
    }

    @Test
    void testReceptionCommandeValidee_CreeLotAutomatiquement() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);

        CommandeFournisseurResponseDTO responseDTO = new CommandeFournisseurResponseDTO();
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class))).thenReturn(responseDTO);

        CommandeFournisseurResponseDTO result = commandeFournisseurService.receptionnerCommande(1L);

        assertNotNull(result);
        verify(lotHelper, times(1)).createLotsFromLignesCommande(commandeValidee);
        verify(lotDAO, times(1)).saveAll(lotsGeneres);
        verify(commandeFournisseurDAO, times(1)).save(argThat(cmd -> cmd.getStatut() == CommandeStatus.LIVREE));
    }

    @Test
    void testReceptionCommande_VerifierGenerationNumeroLot() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lotCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotDAO).saveAll(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getValue();
        assertFalse(lotsSauvegardes.isEmpty());

        Lot lot = lotsSauvegardes.get(0);
        assertNotNull(lot.getNumeroLot(), "Le numéro de lot doit être généré");
        assertTrue(lot.getNumeroLot().startsWith("LOT-"), "Le numéro de lot doit suivre le format attendu");
    }

    @Test
    void testReceptionCommande_VerifierDateEntree() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lotCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotDAO).saveAll(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getValue();
        Lot lot = lotsSauvegardes.get(0);

        assertNotNull(lot.getDateEntree(), "La date d'entrée doit être enregistrée");
        assertEquals(LocalDate.now(), lot.getDateEntree().toLocalDate(), "La date d'entrée doit être la date du jour");
    }

    @Test
    void testReceptionCommande_VerifierPrixAchatUnitaire() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lotCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotDAO).saveAll(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getValue();
        Lot lot = lotsSauvegardes.get(0);

        assertNotNull(lot.getPrixAchatUnitaire(), "Le prix d'achat unitaire doit être enregistré");
        assertEquals(new BigDecimal("15.50"), lot.getPrixAchatUnitaire(),
                "Le prix d'achat unitaire doit correspondre au prix de la ligne de commande");
    }

    @Test
    void testReceptionCommande_VerifierLienAvecCommande() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lotCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotDAO).saveAll(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getValue();
        Lot lot = lotsSauvegardes.get(0);

        assertNotNull(lot.getCommandeFournisseur(), "Le lot doit être lié à la commande fournisseur");
        assertEquals(commandeValidee.getId(), lot.getCommandeFournisseur().getId(),
                "Le lot doit référencer la commande correcte");
    }

    @Test
    void testReceptionCommande_EchecSiCommandeNonValidee() {
        commandeValidee.setStatut(CommandeStatus.EN_ATTENTE);
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            commandeFournisseurService.receptionnerCommande(1L);
        });

        assertEquals("Can only receive VALIDEE orders", exception.getMessage());
        verify(lotDAO, never()).saveAll(anyList());
    }

    @Test
    void testReceptionCommande_EchecSiCommandeInexistante() {
        when(commandeFournisseurDAO.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            commandeFournisseurService.receptionnerCommande(999L);
        });

        assertEquals("Commande not found", exception.getMessage());
        verify(lotDAO, never()).saveAll(anyList());
    }

    @Test
    void testReceptionCommande_ChangementStatutEnLivree() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        ArgumentCaptor<CommandeFournisseur> commandeCaptor = ArgumentCaptor.forClass(CommandeFournisseur.class);
        verify(commandeFournisseurDAO).save(commandeCaptor.capture());

        CommandeFournisseur commandeSauvegardee = commandeCaptor.getValue();
        assertEquals(CommandeStatus.LIVREE, commandeSauvegardee.getStatut(),
                "Le statut de la commande doit passer à LIVREE après réception");
    }

    @Test
    void testReceptionCommande_QuantiteLotCorrespondsLigneCommande() {
        when(commandeFournisseurDAO.findById(1L)).thenReturn(Optional.of(commandeValidee));
        when(lotHelper.createLotsFromLignesCommande(commandeValidee)).thenReturn(lotsGeneres);
        when(commandeFournisseurDAO.save(any(CommandeFournisseur.class))).thenReturn(commandeValidee);
        when(commandeFournisseurMapper.toResponseDto(any(CommandeFournisseur.class)))
                .thenReturn(new CommandeFournisseurResponseDTO());

        commandeFournisseurService.receptionnerCommande(1L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Lot>> lotCaptor = ArgumentCaptor.forClass(List.class);
        verify(lotDAO).saveAll(lotCaptor.capture());

        List<Lot> lotsSauvegardes = lotCaptor.getValue();
        Lot lot = lotsSauvegardes.get(0);

        assertEquals(ligneCommande.getQuantiteCommandee(), lot.getQuantiteInitiale(),
                "La quantité initiale du lot doit correspondre à la quantité commandée");
        assertEquals(ligneCommande.getQuantiteCommandee(), lot.getQuantiteRestante(),
                "La quantité disponible du lot doit être égale à la quantité initiale");
    }
}

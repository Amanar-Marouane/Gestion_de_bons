package com.restapi.gestion_bons.service.commandeFournisseur;

import com.restapi.gestion_bons.contracts.CommandeFournisseurContract;
import com.restapi.gestion_bons.dao.*;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurCreateDTO;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurResponseDTO;
import com.restapi.gestion_bons.dto.commandefournisseur.CommandeFournisseurUpdateDTO;
import com.restapi.gestion_bons.dto.lignecommande.LigneCommandeCreateDTO;
import com.restapi.gestion_bons.entitie.*;
import com.restapi.gestion_bons.entitie.enums.CommandeStatus;
import com.restapi.gestion_bons.mapper.CommandeFournisseurMapper;
import com.restapi.gestion_bons.mapper.LigneCommandeMapper;
import com.restapi.gestion_bons.service.fournisseur.FournisseurService;
import com.restapi.gestion_bons.util.LotHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeFournisseurService implements CommandeFournisseurContract {

    private final CommandeFournisseurDAO commandeFournisseurDAO;
    private final CommandeFournisseurMapper commandeFournisseurMapper;
    private final FournisseurService fournisseurService;
    private final LotDAO lotDAO;
    private final LotHelper lotHelper;
    private final ProduitDAO produitDAO;
    private final LigneCommandeDAO ligneCommandeDAO;
    private final FournisseurDAO fournisseurDAO;
    private final LigneCommandeMapper ligneCommandeMapper;

    public CommandeFournisseurService(
            CommandeFournisseurDAO commandeFournisseurDAO,
            CommandeFournisseurMapper commandeFournisseurMapper,
            FournisseurService fournisseurService, LotDAO lotDAO,
            LotHelper lotHelper,
            ProduitDAO produitDAO,
            LigneCommandeDAO ligneCommandeDAO,
            FournisseurDAO fournisseurDAO,
            LigneCommandeMapper ligneCommandeMapper) {
        this.commandeFournisseurDAO = commandeFournisseurDAO;
        this.commandeFournisseurMapper = commandeFournisseurMapper;
        this.fournisseurService = fournisseurService;
        this.lotDAO = lotDAO;
        this.lotHelper = lotHelper;
        this.produitDAO = produitDAO;
        this.ligneCommandeDAO = ligneCommandeDAO;
        this.fournisseurDAO = fournisseurDAO;
        this.ligneCommandeMapper = ligneCommandeMapper;
    }

    @Transactional
    public CommandeFournisseurResponseDTO save(CommandeFournisseurCreateDTO createDto) {
        if (!fournisseurDAO.existsById(createDto.getFournisseurId())) {
            throw new EntityNotFoundException("Fournisseur not found");
        }

        if (createDto.getLignesCommande() == null || createDto.getLignesCommande().isEmpty()) {
            throw new IllegalArgumentException("Commande must have at least one ligne commande");
        }

        CommandeFournisseur commandeFournisseur = commandeFournisseurMapper.toEntityCreate(createDto);
        CommandeFournisseur saved = commandeFournisseurDAO.save(commandeFournisseur);

        for (LigneCommandeCreateDTO ligneDto : createDto.getLignesCommande()) {
            LigneCommande ligne = ligneCommandeMapper.toEntity(ligneDto);
            ligne.setCommande(saved);

            Produit produit = produitDAO.findById(ligneDto.getProduitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit not found"));
            ligne.setProduit(produit);

            ligneCommandeDAO.save(ligne);
        }

        return commandeFournisseurMapper.toResponseDto(saved);
    }

    @Override
    public CommandeFournisseurResponseDTO update(Long id, CommandeFournisseurUpdateDTO updateDto) {
        CommandeFournisseur existing = commandeFournisseurDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("not found"));
        existing.setDateCommande(updateDto.getDateCommande());
        existing.setMontantTotal(updateDto.getMontantTotal());
        existing.setStatut(updateDto.getStatut());
        if (updateDto.getFournisseurId() != null) {
            Fournisseur fournisseur = new Fournisseur();
            fournisseur.setId(updateDto.getFournisseurId());
            existing.setFournisseur(fournisseur);
        }
        CommandeFournisseur update = commandeFournisseurDAO.save(existing);
        return commandeFournisseurMapper.toResponseDto(update);
    }

    @Override
    public CommandeFournisseurResponseDTO findById(Long id) {
        return commandeFournisseurDAO.findById(id)
                .map(commandeFournisseurMapper::toResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Commande Fournisseur not found whit this Id !"));
    }

    @Override
    public List<CommandeFournisseurResponseDTO> findAll() {
        return commandeFournisseurDAO.findAll().stream()
                .map(commandeFournisseurMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!commandeFournisseurDAO.existsById(id)) {
            throw new EntityNotFoundException("Not Fount with this id");
        }
        commandeFournisseurDAO.deleteById(id);
    }

    @Override
    public List<CommandeFournisseurResponseDTO> findByFournisseurId(Long fournisseurId) {
        if (!fournisseurService.existes(fournisseurId)) {
            throw new EntityNotFoundException("There is no Fournisseur with this id");
        }
        return commandeFournisseurDAO.findByFournisseurId(fournisseurId).stream()
                .map(commandeFournisseurMapper::toResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public CommandeFournisseurResponseDTO receptionnerCommande(Long id) {
        CommandeFournisseur commande = commandeFournisseurDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande not found"));

        if (commande.getStatut() != CommandeStatus.VALIDEE) {
            throw new IllegalStateException("Can only receive VALIDEE orders");
        }

        commande.setStatut(CommandeStatus.LIVREE);

        List<Lot> lots = lotHelper.createLotsFromLignesCommande(commande);
        lotDAO.saveAll(lots);

        CommandeFournisseur saved = commandeFournisseurDAO.save(commande);
        return commandeFournisseurMapper.toResponseDto(saved);
    }

    public CommandeFournisseurResponseDTO valideCommande(Long id) {
        CommandeFournisseur commande = commandeFournisseurDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande not found"));

        if (commande.getStatut() != CommandeStatus.EN_ATTENTE) {
            throw new IllegalStateException("Can only receive EN_ATTENTE orders");
        }

        commande.setStatut(CommandeStatus.VALIDEE);
        commandeFournisseurDAO.save(commande);
        return commandeFournisseurMapper.toResponseDto(commande);
    }
}

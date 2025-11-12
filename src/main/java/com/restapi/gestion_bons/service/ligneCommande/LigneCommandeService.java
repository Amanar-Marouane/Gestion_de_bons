package com.restapi.gestion_bons.service.ligneCommande;

import com.restapi.gestion_bons.contracts.LigneCommandeContract;
import com.restapi.gestion_bons.dao.CommandeFournisseurDAO;
import com.restapi.gestion_bons.dao.LigneCommandeDAO;
import com.restapi.gestion_bons.dao.ProduitDAO;
import com.restapi.gestion_bons.dto.lignecommande.LigneCommandeCreateDTO;
import com.restapi.gestion_bons.dto.lignecommande.LigneCommandeResponseDTO;
import com.restapi.gestion_bons.dto.lignecommande.LigneCommandeUpdateDTO;
import com.restapi.gestion_bons.entitie.CommandeFournisseur;
import com.restapi.gestion_bons.entitie.LigneCommande;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.mapper.LigneCommandeMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class LigneCommandeService implements LigneCommandeContract {

    private final LigneCommandeDAO ligneCommandeDAO;
    private final LigneCommandeMapper ligneCommandeMapper;
    private final CommandeFournisseurDAO commandeFournisseurDAO;
    private final ProduitDAO produitDAO;

    public LigneCommandeService(
            LigneCommandeMapper ligneCommandeMapper,
            LigneCommandeDAO ligneCommandeDAO,
            CommandeFournisseurDAO commandeFournisseurDAO, ProduitDAO produitDAO){
        this.ligneCommandeDAO = ligneCommandeDAO;
        this.ligneCommandeMapper = ligneCommandeMapper;
        this.commandeFournisseurDAO = commandeFournisseurDAO;
        this.produitDAO = produitDAO;
    }


    @Override
    public LigneCommandeResponseDTO save(LigneCommandeCreateDTO createDTO) {
        if(createDTO.getCommandeId() == null || createDTO.getProduitId() == null){
            throw new IllegalArgumentException("produit id ou commannde id vide!");
        }

        CommandeFournisseur commandeFournisseur = commandeFournisseurDAO.findById(createDTO.getCommandeId())
                .orElseThrow(() -> new EntityNotFoundException("No commande avec cette id !"));

        Produit produit = produitDAO.findById(createDTO.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("No commande avec cette id !"));

        LigneCommande ligneCommande = ligneCommandeMapper.toEntity(createDTO);
        ligneCommande.setCommande(commandeFournisseur);
        ligneCommande.setProduit(produit);

        LigneCommande saved = ligneCommandeDAO.save(ligneCommande);

        return ligneCommandeMapper.toResponseDto(saved);
    }

    @Override
    public LigneCommandeResponseDTO update(Long id, LigneCommandeUpdateDTO updateDTO) {

        if(updateDTO.getCommandeId() == null || updateDTO.getProduitId() == null){
            throw new IllegalArgumentException("produit id ou commannde id vide!");
        }
        LigneCommande ligneExist = ligneCommandeDAO.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pas de ligne de commande avec cette id !"));

        CommandeFournisseur commandeFournisseur = commandeFournisseurDAO.findById(updateDTO.getCommandeId())
                .orElseThrow(() -> new EntityNotFoundException("No commande avec cette id !"));

        Produit produit = produitDAO.findById(updateDTO.getProduitId())
                .orElseThrow(() -> new EntityNotFoundException("No commande avec cette id !"));

        ligneExist.setCommande(commandeFournisseur);
        ligneExist.setProduit(produit);
        ligneExist.setQuantiteCommandee(updateDTO.getQuantiteCommandee());
        ligneExist.setPrixAchatUnitaire(updateDTO.getPrixAchatUnitaire());
        return ligneCommandeMapper.toResponseDto(ligneCommandeDAO.save(ligneExist));
    }

    @Override
    public Optional<LigneCommandeResponseDTO> findById(Long id) {
        if (id == null || id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id not Valid");
       }
        return ligneCommandeDAO.findById(id)
                .map(ligneCommandeMapper::toResponseDto);

    }

    @Override
    public List<LigneCommandeResponseDTO> findAll() {
        List<LigneCommande> ligneCommandes= ligneCommandeDAO.findAll();
        if (ligneCommandes.isEmpty()){
            throw new EntityNotFoundException("There is no Ligne de Commandes");
        }
        return ligneCommandeMapper.toResponseDtoList(ligneCommandes);
    }

    @Override
    public void delete(Long id) {
        if (!ligneCommandeDAO.existsById(id)){
            throw new EntityNotFoundException("No ligne de commande avec cette id !");
        }
        ligneCommandeDAO.deleteById(id);

    }

    public List<LigneCommandeResponseDTO> getLigneCommandeByProduitId(Long id){
        if(!produitDAO.existsById(id)){
            throw new EntityNotFoundException("There is no Produt with is id");
        }

        List<LigneCommandeResponseDTO> dtos =  ligneCommandeDAO.findLigneCommandeByProduit_Id(id)
                .stream()
                .map(ligneCommandeMapper::toResponseDto)
                .toList();
        if (dtos.isEmpty()){
            throw new EntityNotFoundException("There is no Ligne Commande with this product id");
        }
        return dtos;
    }

    public List<LigneCommandeResponseDTO> getLigneCommandeByFournisseurCommandeId(Long id){
        if(!commandeFournisseurDAO.existsById(id)){
            throw new EntityNotFoundException("There is no Commande Fournisseur with is id");
        }

        List<LigneCommandeResponseDTO> dtos = ligneCommandeDAO.findLigneCommandeByCommande_Fournisseur_Id(id)
                .stream()
                .map(ligneCommandeMapper::toResponseDto)
                .toList();
        if (dtos.isEmpty()){
            throw new EntityNotFoundException("There is no Ligne Commande with this commande fournisseur id");
        }
        return dtos;
    }
}

package com.restapi.gestion_bons.service.produit;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.restapi.gestion_bons.contracts.ProduitServiceContract;
import com.restapi.gestion_bons.dao.ProduitDAO;
import com.restapi.gestion_bons.entitie.Produit;
import com.restapi.gestion_bons.dto.produit.ProduitResponseDTO;
import com.restapi.gestion_bons.dto.produit.ProduitRequestDTO;
import com.restapi.gestion_bons.exception.DuplicateResourceException;
import com.restapi.gestion_bons.mapper.ProduitMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@Primary
@RequiredArgsConstructor
public class ProduitService implements ProduitServiceContract {
    private final ProduitDAO produitDAO;
    private final ProduitMapper produitMapper;

    @Override
    public Page<ProduitResponseDTO> findAllWithPagination(Pageable pageable) {
        return produitDAO.findAll(pageable).map(produitMapper::toResponseDto);
    }

    @Override
    public List<ProduitResponseDTO> findAll() {
        return produitDAO.findAll().stream().map(produitMapper::toResponseDto).toList();
    }

    public Optional<ProduitResponseDTO> findById(Long id) {
        return produitDAO.findById(id).map(produitMapper::toResponseDto);
    }

    public ProduitResponseDTO save(ProduitRequestDTO dto) {
        if (dto.getReference() != null && produitDAO.findByReference(dto.getReference()) != null) {
            throw new DuplicateResourceException("reference", dto.getReference());
        }
        if (dto.getNom() != null && produitDAO.findByNom(dto.getNom()) != null) {
            throw new DuplicateResourceException("name", dto.getNom());
        }
        Produit p = produitMapper.toEntity(dto);
        Produit saved = produitDAO.save(p);
        return produitMapper.toResponseDto(saved);
    }

    public ProduitResponseDTO update(Long id, ProduitRequestDTO dto) {
        return produitDAO.findById(id).map(existing -> {
            // if reference changed, ensure new reference is not taken
            if (dto.getReference() != null && !dto.getReference().equals(existing.getReference())) {
                Produit byRef = produitDAO.findByReference(dto.getReference());
                if (byRef != null && !byRef.getId().equals(id)) {
                    throw new DuplicateResourceException("reference", dto.getReference());
                }
            }
            // if name changed, ensure new name is not taken
            if (dto.getNom() != null && !dto.getNom().equals(existing.getNom())) {
                Produit byName = produitDAO.findByNom(dto.getNom());
                if (byName != null && !byName.getId().equals(id)) {
                    throw new DuplicateResourceException("name", dto.getNom());
                }
            }

            Produit updated = produitMapper.toEntity(dto);
            existing.setReference(updated.getReference());
            existing.setNom(updated.getNom());
            existing.setDescription(updated.getDescription());
            existing.setCategorie(updated.getCategorie());
            existing.setUniteMesure(updated.getUniteMesure());
            Produit saved = produitDAO.save(existing);
            return produitMapper.toResponseDto(saved);
        }).orElseThrow(() -> new NoSuchElementException("Produit not found with id " + id));
    }

    public void delete(Long id) {
        produitDAO.deleteById(id);
    }

    public Optional<ProduitResponseDTO> findByNom(String name) {
        return Optional.ofNullable(produitDAO.findByNom(name)).map(produitMapper::toResponseDto);
    }

    public Optional<ProduitResponseDTO> findByReference(String reference) {
        return Optional.ofNullable(produitDAO.findByReference(reference)).map(produitMapper::toResponseDto);
    }

    public List<ProduitResponseDTO> findByCategorie(String category) {
        return produitDAO.findByCategorie(category).stream().map(produitMapper::toResponseDto).toList();
    }

//    @PostConstruct
    public List<ProduitResponseDTO> initDB(){
        List<ProduitRequestDTO> produits = IntStream.rangeClosed(1, 200)
                .mapToObj(i -> ProduitRequestDTO.builder()
                        .reference("REF-" + String.format("%03d", i))
                        .nom("Produit " + i)
                        .description("Description du produit " + i)
                        .categorie("Categorie " + (i % 5 + 1))
                        .uniteMesure("PIECE")
                        .reorderPoint(new Random().nextInt(50) + 10)
                        .build())
                .toList();

        return produits.stream()
                .map(this::save)
                .toList();
    }
}

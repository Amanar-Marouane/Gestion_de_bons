package com.restapi.gestion_bons.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieCreateDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieResponseDTO;
import com.restapi.gestion_bons.dto.bondesortie.BonDeSortieUpdateDTO;
import com.restapi.gestion_bons.entitie.BonDeSortie;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AtelierMapper.class, BonDeSortieLigneMapper.class })
public interface BonDeSortieMapper {

    @Mapping(target = "atelier.nom", source = "atelier.nom")
    @Mapping(target = "atelier.id", source = "atelier.id")
    BonDeSortieResponseDTO toResponseDto(BonDeSortie bonDeSortie);

    @Mapping(target = "atelier.id", source = "atelierId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroBon", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "bonDeSortieLignes", ignore = true)
    BonDeSortie toEntity(BonDeSortieCreateDTO dto);

    @Mapping(target = "atelier.id", source = "atelierId")
    @Mapping(target = "bonDeSortieLignes", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroBon", ignore = true)
    BonDeSortie toEntity(BonDeSortieUpdateDTO dto);

    List<BonDeSortieResponseDTO> toResponseDtoList(List<BonDeSortie> bonsDeSortie);
    
}

package com.restapi.gestion_bons.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockRequestDTO;
import com.restapi.gestion_bons.dto.mouvementstock.MouvementStockResponseDTO;
import com.restapi.gestion_bons.entitie.MouvementStock;

import java.util.List;

@Mapper(componentModel = "spring", uses = { ProduitMapper.class })
public interface MouvementStockMapper {

    @Mapping(target = "lot.numeroLot", source = "lot.numeroLot")
    @Mapping(target = "lot.id", source = "lot.id")
    MouvementStockResponseDTO toResponseDto(MouvementStock mouvementStock);

    @Mapping(target = "produit.id", source = "produitId")
    @Mapping(target = "lot.id", source = "lotId")
    MouvementStock toEntity(MouvementStockRequestDTO dto);

    List<MouvementStockResponseDTO> toResponseDtoList(List<MouvementStock> mouvements);
}

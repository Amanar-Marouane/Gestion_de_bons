package com.restapi.gestion_bons.specification;

import com.restapi.gestion_bons.entitie.MouvementStock;
import com.restapi.gestion_bons.entitie.enums.TypeMouvement;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MouvementStockSpecification {

    public static Specification<MouvementStock> withCriteria(
            Long produitId,
            Long lotId,
            TypeMouvement type,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (produitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("produit").get("id"), produitId));
            }

            if (lotId != null) {
                predicates.add(criteriaBuilder.equal(root.get("lot").get("id"), lotId));
            }

            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("typeMouvement"), type));
            }

            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("dateMouvement"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dateMouvement"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dateMouvement"), endDate));
            }

            // Order by dateMouvement DESC
            query.orderBy(criteriaBuilder.desc(root.get("dateMouvement")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

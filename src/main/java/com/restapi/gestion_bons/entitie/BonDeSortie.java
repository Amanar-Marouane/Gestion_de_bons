package com.restapi.gestion_bons.entitie;

import com.restapi.gestion_bons.entitie.enums.BonDeSortieStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;

@Entity
@Builder
@Table(name = "bon_de_sortie")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(of = "id")
public class BonDeSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="numero_bon", nullable = false)
    private String numeroBon;

    @Column(name = "date_sortie", nullable = false)
    private Date dateSortie;

    @Column(name = "motif_sortie", nullable = false)
    private String motifSortie;

    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    private BonDeSortieStatus statut;

    @OneToMany(mappedBy = "bonDeSortie", fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<BonDeSortieLigne> bonDeSortieLignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atelier_destinataire_id", nullable = false)
    private Atelier atelier;

}

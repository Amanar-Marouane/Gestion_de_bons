package com.restapi.gestion_bons.seeder;

import com.restapi.gestion_bons.dao.*;
import com.restapi.gestion_bons.entitie.*;
import com.restapi.gestion_bons.entitie.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed") // Only run when --spring.profiles.active=seed
public class DatabaseSeeder implements CommandLineRunner {

    private final ProduitDAO produitDAO;
    private final FournisseurDAO fournisseurDAO;
    private final CommandeFournisseurDAO commandeFournisseurDAO;
    private final LotDAO lotDAO;
    private final AtelierDAO atelierDAO;
    private final BonDeSortieDAO bonDeSortieDAO;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🌱 Starting database seeding...");

        seedProduits();
        seedFournisseurs();
        seedCommandesFournisseurs();
        seedLots();
        seedAteliers();
        seedBonsDeSortie();

        log.info("✅ Database seeding completed successfully!");
    }

    private void seedProduits() {
        if (produitDAO.count() > 0) {
            log.info("⏭️  Produits already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Produits...");

        List<Produit> produits = List.of(
                Produit.builder()
                        .reference("TIS-001")
                        .nom("Tissu Coton Blanc")
                        .description("Tissu coton 100% pour uniformes")
                        .categorie("Tissus")
                        .uniteMesure("mètre")
                        .reorderPoint(100)
                        .build(),
                Produit.builder()
                        .reference("TIS-002")
                        .nom("Tissu Polyester Bleu")
                        .description("Tissu polyester résistant")
                        .categorie("Tissus")
                        .uniteMesure("mètre")
                        .reorderPoint(80)
                        .build(),
                Produit.builder()
                        .reference("FIL-001")
                        .nom("Fil Noir Standard")
                        .description("Fil de couture noir polyester")
                        .categorie("Fournitures")
                        .uniteMesure("bobine")
                        .reorderPoint(50)
                        .build(),
                Produit.builder()
                        .reference("BTN-001")
                        .nom("Boutons Plastique 15mm")
                        .description("Boutons plastique noir 4 trous")
                        .categorie("Accessoires")
                        .uniteMesure("pièce")
                        .reorderPoint(200)
                        .build(),
                Produit.builder()
                        .reference("FER-001")
                        .nom("Fermeture Éclair 20cm")
                        .description("Fermeture éclair métal 20cm")
                        .categorie("Accessoires")
                        .uniteMesure("pièce")
                        .reorderPoint(150)
                        .build());

        produitDAO.saveAll(produits);
        log.info("✅ {} Produits seeded", produits.size());
    }

    private void seedFournisseurs() {
        if (fournisseurDAO.count() > 0) {
            log.info("⏭️  Fournisseurs already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Fournisseurs...");

        List<Fournisseur> fournisseurs = List.of(
                Fournisseur.builder()
                        .raisonSociale("Textiles Maroc SARL")
                        .addressComplete("Zone Industrielle Ain Sebaa, Casablanca")
                        .personneContact("Hassan Bennani")
                        .email("contact@textiles-maroc.ma")
                        .telephone("+212522334455")
                        .ville("Casablanca")
                        .ice("001234567890123")
                        .build(),
                Fournisseur.builder()
                        .raisonSociale("Fournitures Pro")
                        .addressComplete("Avenue Mohammed V, Rabat")
                        .personneContact("Fatima Alaoui")
                        .email("info@fournitures-pro.ma")
                        .telephone("+212537556677")
                        .ville("Rabat")
                        .ice("002345678901234")
                        .build(),
                Fournisseur.builder()
                        .raisonSociale("Atlas Accessoires")
                        .addressComplete("Quartier Industriel, Tanger")
                        .personneContact("Karim Tazi")
                        .email("k.tazi@atlas-access.ma")
                        .telephone("+212539887766")
                        .ville("Tanger")
                        .ice("003456789012345")
                        .build());

        fournisseurDAO.saveAll(fournisseurs);
        log.info("✅ {} Fournisseurs seeded", fournisseurs.size());
    }

    private void seedCommandesFournisseurs() {
        if (commandeFournisseurDAO.count() > 0) {
            log.info("⏭️  Commandes Fournisseurs already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Commandes Fournisseurs...");

        List<Fournisseur> fournisseurs = fournisseurDAO.findAll();
        List<Produit> produits = produitDAO.findAll();

        CommandeFournisseur commande1 = CommandeFournisseur.builder()
                .dateCommande(new Date())
                .montantTotal(15000.0)
                .statut(CommandeStatus.LIVREE)
                .fournisseur(fournisseurs.get(0))
                .build();

        List<LigneCommande> lignes1 = new ArrayList<>();
        lignes1.add(LigneCommande.builder()
                .commande(commande1)
                .produit(produits.get(0))
                .quantiteCommandee(200)
                .prixAchatUnitaire(50.0)
                .build());
        lignes1.add(LigneCommande.builder()
                .commande(commande1)
                .produit(produits.get(1))
                .quantiteCommandee(150)
                .prixAchatUnitaire(60.0)
                .build());
        commande1.setLignesCommande(lignes1);

        CommandeFournisseur commande2 = CommandeFournisseur.builder()
                .dateCommande(new Date())
                .montantTotal(8000.0)
                .statut(CommandeStatus.LIVREE)
                .fournisseur(fournisseurs.get(1))
                .build();

        List<LigneCommande> lignes2 = new ArrayList<>();
        lignes2.add(LigneCommande.builder()
                .commande(commande2)
                .produit(produits.get(2))
                .quantiteCommandee(100)
                .prixAchatUnitaire(25.0)
                .build());
        lignes2.add(LigneCommande.builder()
                .commande(commande2)
                .produit(produits.get(3))
                .quantiteCommandee(500)
                .prixAchatUnitaire(5.0)
                .build());
        commande2.setLignesCommande(lignes2);

        commandeFournisseurDAO.saveAll(List.of(commande1, commande2));
        log.info("✅ {} Commandes Fournisseurs seeded", 2);
    }

    private void seedLots() {
        if (lotDAO.count() > 0) {
            log.info("⏭️  Lots already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Lots...");

        List<Produit> produits = produitDAO.findAll();
        List<CommandeFournisseur> commandes = commandeFournisseurDAO.findAll();

        List<Lot> lots = List.of(
                Lot.builder()
                        .numeroLot("LOT-2025-0001")
                        .dateEntree(LocalDateTime.now().minusDays(30))
                        .quantiteInitiale(200)
                        .quantiteRestante(150)
                        .prixAchatUnitaire(new BigDecimal("50.00"))
                        .statut(LotStatus.DISPONIBLE)
                        .produit(produits.get(0))
                        .commandeFournisseur(commandes.get(0))
                        .build(),
                Lot.builder()
                        .numeroLot("LOT-2025-0002")
                        .dateEntree(LocalDateTime.now().minusDays(25))
                        .quantiteInitiale(150)
                        .quantiteRestante(120)
                        .prixAchatUnitaire(new BigDecimal("60.00"))
                        .statut(LotStatus.DISPONIBLE)
                        .produit(produits.get(1))
                        .commandeFournisseur(commandes.get(0))
                        .build(),
                Lot.builder()
                        .numeroLot("LOT-2025-0003")
                        .dateEntree(LocalDateTime.now().minusDays(20))
                        .quantiteInitiale(100)
                        .quantiteRestante(80)
                        .prixAchatUnitaire(new BigDecimal("25.00"))
                        .statut(LotStatus.DISPONIBLE)
                        .produit(produits.get(2))
                        .commandeFournisseur(commandes.get(1))
                        .build(),
                Lot.builder()
                        .numeroLot("LOT-2025-0004")
                        .dateEntree(LocalDateTime.now().minusDays(15))
                        .quantiteInitiale(500)
                        .quantiteRestante(450)
                        .prixAchatUnitaire(new BigDecimal("5.00"))
                        .statut(LotStatus.DISPONIBLE)
                        .produit(produits.get(3))
                        .commandeFournisseur(commandes.get(1))
                        .build(),
                Lot.builder()
                        .numeroLot("LOT-2025-0005")
                        .dateEntree(LocalDateTime.now().minusDays(10))
                        .quantiteInitiale(300)
                        .quantiteRestante(280)
                        .prixAchatUnitaire(new BigDecimal("8.50"))
                        .statut(LotStatus.DISPONIBLE)
                        .produit(produits.get(4))
                        .commandeFournisseur(commandes.get(0))
                        .build(),
                // Lot épuisé pour test
                Lot.builder()
                        .numeroLot("LOT-2025-0006")
                        .dateEntree(LocalDateTime.now().minusDays(35))
                        .quantiteInitiale(50)
                        .quantiteRestante(0)
                        .prixAchatUnitaire(new BigDecimal("48.00"))
                        .statut(LotStatus.EPUISE)
                        .produit(produits.get(0))
                        .commandeFournisseur(commandes.get(0))
                        .build());

        lotDAO.saveAll(lots);
        log.info("✅ {} Lots seeded", lots.size());
    }

    private void seedAteliers() {
        if (atelierDAO.count() > 0) {
            log.info("⏭️  Ateliers already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Ateliers...");

        List<Atelier> ateliers = List.of(
                Atelier.builder()
                        .nom("Atelier Couture - Bâtiment A")
                        .build(),
                Atelier.builder()
                        .nom("Atelier Finition - Bâtiment B")
                        .build(),
                Atelier.builder()
                        .nom("Atelier Découpe - Bâtiment C")
                        .build(),
                Atelier.builder()
                        .nom("Atelier Repassage - Bâtiment A")
                        .build());

        atelierDAO.saveAll(ateliers);
        log.info("✅ {} Ateliers seeded", ateliers.size());
    }

    private void seedBonsDeSortie() {
        if (bonDeSortieDAO.count() > 0) {
            log.info("⏭️  Bons de Sortie already seeded, skipping...");
            return;
        }

        log.info("🌱 Seeding Bons de Sortie...");

        List<Atelier> ateliers = atelierDAO.findAll();
        List<Produit> produits = produitDAO.findAll();

        BonDeSortie bon1 = BonDeSortie.builder()
                .numeroBon("BS-000001")
                .dateSortie(new Date())
                .motifSortie("PRODUCTION - Commande client #1234")
                .statut(BonDeSortieStatus.BROUILLON)
                .atelier(ateliers.get(0))
                .build();

        List<BonDeSortieLigne> lignes1 = new ArrayList<>();
        lignes1.add(BonDeSortieLigne.builder()
                .bonDeSortie(bon1)
                .produit(produits.get(0))
                .quantiteDemandee(50)
                .build());
        lignes1.add(BonDeSortieLigne.builder()
                .bonDeSortie(bon1)
                .produit(produits.get(2))
                .quantiteDemandee(20)
                .build());
        bon1.setBonDeSortieLignes(lignes1);

        BonDeSortie bon2 = BonDeSortie.builder()
                .numeroBon("BS-000002")
                .dateSortie(new Date())
                .motifSortie("MAINTENANCE - Réparation équipement")
                .statut(BonDeSortieStatus.BROUILLON)
                .atelier(ateliers.get(1))
                .build();

        List<BonDeSortieLigne> lignes2 = new ArrayList<>();
        lignes2.add(BonDeSortieLigne.builder()
                .bonDeSortie(bon2)
                .produit(produits.get(3))
                .quantiteDemandee(100)
                .build());
        bon2.setBonDeSortieLignes(lignes2);

        bonDeSortieDAO.saveAll(List.of(bon1, bon2));
        log.info("✅ {} Bons de Sortie seeded", 2);
    }
}

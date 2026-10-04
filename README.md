# SIREN Loader

## Principe

Pour chaque dataset déclaré dans `application.yml` :

1. Lit le **descripteur** (`datasets/csv/*.csv`, colonnes `Nom,Libellé,Longueur,Type,Ordre`).
2. Crée la table si elle n'existe pas (`CREATE TABLE IF NOT EXISTS`). Une table existante n'est jamais modifiée.
3. Vide la table (`TRUNCATE`) puis la recharge par `COPY … FROM STDIN (FORMAT csv, HEADER true)`, dans une transaction (rollback en cas d'erreur).

Le CSV de données n'est jamais parsé en Java : il est streamé tel quel vers PostgreSQL. Le `COPY` suppose que l'ordre des colonnes du CSV est celui du descripteur.

### Types SQL

| Descripteur | SQL |
| --- | --- |
| `Texte`, `Liste de codes` | `varchar(Longueur)` (`text` si longueur `null`) |
| `Date`, longueur 10 | `date` |
| `Date`, longueur 23 | `timestamp` |
| `Date`, longueur 4 (colonnes « année ») | `smallint` |
| `Numérique` | `integer` |

Les noms de colonnes sont ceux du descripteur, en minuscules. Aucune clé ni index n'est créé (chargement plus rapide) : à ajouter après coup si besoin.

## Datasets

| Dataset | Descripteur | Table |
| --- | --- | --- |
| `StockDoublons_utf8.csv` | `stockdoublons-311-dessin-de-fichier.csv` | `te_doublons` |
| `StockEtablissement_utf8.csv` | `stocketablissement-311-dessin-de-fichier.csv` | `te_etablissement` |
| `StockEtablissementHistorique_utf8.csv` | `liste-csv-des-variables-du-dessin-du-fichier-stocketablissementhistorique-311.csv` | `te_etablissement_historique` |
| `StockEtablissementLiensSuccession_utf8.csv` | `liste-csv-des-variables-du-dessin-du-fichier-stocketablissementlienssuccession-311.csv` | `te_etablissement_liens_succession` |
| `StockUniteLegale_utf8.csv` | `stockunitelegale-311-dessin-de-fichier.csv` | `te_unite_legale` |
| `StockUniteLegaleHistorique_utf8.csv` | `stockunitelegalehistorique-311-dessin-de-fichier.csv` | `te_unite_legale_historique` |

Les descripteurs PDF correspondants sont dans `documentation/`.

## Structure

```
├── pom.xml
├── docker-compose.yml          # PostgreSQL 16 (conteneur sirene-db, volume sirene_data)
├── context.md                  # cadrage du projet
├── documentation/              # descripteurs PDF INSEE
├── datasets/
│   ├── Stock*_utf8.csv         # données à charger (~33 Go)
│   ├── archive/                # zips INSEE d'origine
│   └── csv/                    # descripteurs au format csv (schéma des tables)
└── src/main
    ├── java/org/dfc/sirene/
    │   ├── SireneLoaderApplication.java
    │   ├── SireneProperties.java   # binding de la config `sirene.*`
    │   └── SireneLoader.java       # lecture descripteur, création de table, COPY
    └── resources/application.yml
```

## Utilisation

```bash
cp .env.example .env                    # puis y renseigner DB_PASSWORD
docker compose up -d                    # démarre PostgreSQL (lit .env)
export DB_PASSWORD=...                  # même valeur, pour l'application (Spring ne lit pas .env)
mvn spring-boot:run                     # ou : mvn package && java -jar target/sirene-loader-0.0.1-SNAPSHOT.jar
```

À lancer depuis la racine du projet (les chemins `datasets/` sont relatifs).

## Configuration (`src/main/resources/application.yml`)

| Clé | Rôle |
| --- | --- |
| `spring.datasource.url / username / password` | connexion PostgreSQL (mot de passe obligatoire via la variable d'environnement `DB_PASSWORD`) |
| `sirene.datasets-dir` | dossier des CSV de données |
| `sirene.descriptors-dir` | dossier des descripteurs CSV |
| `sirene.datasets` | liste `dataset` / `descriptor` / `table` |

## Points d'attention

- **Rechargement complet :** chaque lancement vide puis recharge toutes les tables.
- **Évolution du schéma :** si un descripteur change, supprimer la table (`DROP TABLE`) avant de relancer, sinon l'ancien schéma est conservé.
- **Doublons :** `StockDoublons` va dans `te_doublons` (et non `te_etablissement`, dont la structure est différente).
- **Secrets :** le mot de passe n'est pas versionné : `DB_PASSWORD` (fichier `.env` ignoré par git, modèle dans `.env.example`).

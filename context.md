# SIREN LOADER

## framework
* java 25
* springboot 4.1

## données en entrées

### url


| dataset | descrpiteur | table cible |
| ------- | ----------- | ----------- |
| StockDoublons_utf8.csv | stockdoublons-311-description-fichier-29-01-2026.pdf | te_etablissement |
| StockEtablissement_utf8.csv | stocketablissement-311-description-fichier-29-01-2026.pdf | te_etablissement |
| StockEtablissementHistorique_utf8.csv | stocketablissementhistorique-311-description-fichier-29-01-2026.pdf | te_etablissement_historique | 
| StockEtablissementLiensSuccession_utf8.csv | stocketablissementlienssuccession-311-description-fichier-29-01-2026.pdf | te_etablissement_liens_succession | 
| StockUniteLegale_utf8.csv | stockunitelegale-311-description-fichier-29-01-2026.pdf | te_unite_legale | 
| StockUniteLegaleHistorique_utf8.csv | stockunitelegalehistorique-311-description-fichier-29-01-2026.pdf | te_unite_legale_historique | 

### explications
- dataset     : fichier csv avec les données à charger. la première ligne du csv contient les entête des colonnes.
- descrpiteur : fichier pdf qui décrit la structure du dataset (csv).
- table cible : nom de la table attendue dans la base de données.

## structure du projet

```
dfc-sirene-loader/
├── context.md              # ce fichier : cadrage du projet
├── docker-compose.yml      # base PostgreSQL 16 (sirene-db, volume sirene_data)
├── documentation/          # descripteurs PDF INSEE (structure de chaque dataset)
├── datasets/
│   ├── Stock*_utf8.csv     # données à charger (6 fichiers)
│   ├── archive/            # zips INSEE d'origine
│   └── csv/                # dessins de fichier (liste des variables) au format csv
└── .claude/settings.json   # configuration Claude Code
```

Le code Java/Spring Boot (sources, `pom.xml`/`build.gradle`) n'existe pas encore.

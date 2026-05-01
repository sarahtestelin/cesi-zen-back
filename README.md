# CESIZen Back-End

L’application a pour objectif de proposer une plateforme autour de la santé mentale, avec des fonctionnalités de gestion de comptes, de contenus informatifs et de diagnostic de stress.
Elle est développée en Java/Spring Boot .

## Technologies utilisées

- Java 21
- Spring Boot
- Spring Security
- JWT/Refresh Token
- Spring Data JPA pour l'ORM
- PostGreSQL
- JUnit
- Mockito
- MockMvc
- JaCoco

## Fonctionnalités principales

### Authentification et comptes utilisateurs

- Création de compte
- Connexion
- Déconnexion
- Gestion des tokens JWT et refresh tokens
- Réinitialisation du mot de passe
- Gestion du profil utilisateur
- Suppression/anonymisation du compte
- Gestion des rôles utilisateur et administrateur

### Les ressources sur la santé mentale

- Consultation des ressources
- Création de ressources par l’administrateur
- Modification des ressources
- Désactivation des ressources
- Historisation des changements

### Diagnostic de stress

- Consultation du questionnaire
- Calcul du score de stress
- Sauvegarde des résultats pour les utilisateurs connectés
- Consultation de l’historique des diagnostics
- Purge automatique des anciens résultats pour le RGPD

### Sécurité

- Authentification par JWT
- Refresh token sécurisé
- Protection des endpoints selon les rôles
- Hashage des mots de passe avec Bcrypt
- Limitation des tentatives de connexion
- Gestion des accès administrateur
- Anonymisation des données utilisateur

## Pour lancer le projet

Il faut avoir installé :

* Java 21
* Maven
* PostGreSQL

## Installation

Cloner le projet :

```bash
git clone <https://github.com/sarahtestelin/cesi-zen-back>
cd cesi-zen-back
```

Installer les dépendances :

Sur Mac :
```bash
./mvnw clean install
```

Sur Windows :

```bash
mvnw.cmd clean install
```

## Configuration de la base de données

Créer une base PostGreSQL :

```sql
CREATE DATABASE cesi_zen;
```

Configurer ensuite le fichier :

```txt
src/main/resources/application.yaml
```


## Lancement du projet

Lancer l’application avec Maven :

Sur Mac :
```bash
./mvnw spring-boot:run
```

Sur Windows :

```bash
mvnw.cmd spring-boot:run
```

Le Swagger est accessible à l’adresse :

```txt
https://localhost:8443/swagger-ui/index.html```
```

## Lancer les tests

Pour lancer tous les tests :

Sur Mac :
```bash
./mvnw test
```

Sur Windows :

```bash
mvnw.cmd test
```

## Couverture de tests

Le projet utilise JaCoCo pour mesurer la couverture de tests.

Pour générer le rapport :

```bash
./mvnw clean test jacoco:report
```

Le rapport est ensuite disponible ici :

```txt
target/site/jacoco/index.html
```

## Types de tests présents

Le projet contient plusieurs types de tests :

- Tests unitaires
- Tests fonctionnels
- Tests de non-régression

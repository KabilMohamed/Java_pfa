package com.pharmacie.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Utilitaire pour la gestion des dates
 * Fournit des méthodes helper pour formater, parser et manipuler les dates
 */
public class DateUtil {
    
    // Formatters prédéfinis
    public static final DateTimeFormatter FORMAT_ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter FORMAT_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMAT_US = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    public static final DateTimeFormatter FORMAT_LONG_FR = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    public static final DateTimeFormatter FORMAT_DATETIME_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter FORMAT_DATETIME_ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Formate une date au format français (jj/mm/aaaa)
     * 
     * @param date Date à formater
     * @return Date formatée
     */
    public static String formaterDateFr(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMAT_FR);
    }
    
    /**
     * Formate une date au format ISO (aaaa-mm-jj)
     * 
     * @param date Date à formater
     * @return Date formatée
     */
    public static String formaterDateIso(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMAT_ISO);
    }
    
    /**
     * Formate une date au format long français (ex: 15 janvier 2025)
     * 
     * @param date Date à formater
     * @return Date formatée
     */
    public static String formaterDateLongue(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMAT_LONG_FR);
    }
    
    /**
     * Formate une date-heure au format français
     * 
     * @param dateTime Date-heure à formater
     * @return Date-heure formatée
     */
    public static String formaterDateTimeFr(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(FORMAT_DATETIME_FR);
    }
    
    /**
     * Parse une date depuis le format français
     * 
     * @param dateStr Date au format jj/mm/aaaa
     * @return LocalDate parsée
     * @throws DateTimeParseException Si le format est invalide
     */
    public static LocalDate parserDateFr(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, FORMAT_FR);
    }
    
    /**
     * Parse une date depuis le format ISO
     * 
     * @param dateStr Date au format aaaa-mm-jj
     * @return LocalDate parsée
     * @throws DateTimeParseException Si le format est invalide
     */
    public static LocalDate parserDateIso(String dateStr) throws DateTimeParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, FORMAT_ISO);
    }
    
    /**
     * Parse une date avec tentative de multiples formats
     * 
     * @param dateStr Date à parser
     * @return LocalDate parsée ou null si échec
     */
    public static LocalDate parserDateAuto(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        // Essayer différents formats
        DateTimeFormatter[] formats = {
            FORMAT_ISO,
            FORMAT_FR,
            FORMAT_US,
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        
        for (DateTimeFormatter format : formats) {
            try {
                return LocalDate.parse(dateStr, format);
            } catch (DateTimeParseException e) {
                // Continuer avec le format suivant
            }
        }
        
        return null;
    }
    
    /**
     * Calcule le nombre de jours entre deux dates
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Nombre de jours
     */
    public static long joursEntre(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(debut, fin);
    }
    
    /**
     * Calcule le nombre de mois entre deux dates
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Nombre de mois
     */
    public static long moisEntre(LocalDate debut, LocalDate fin) {
        if (debut == null || fin == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(debut, fin);
    }
    
    /**
     * Calcule le nombre de jours depuis une date
     * 
     * @param date Date de référence
     * @return Nombre de jours depuis cette date
     */
    public static long joursDepuis(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(date, LocalDate.now());
    }
    
    /**
     * Calcule le nombre de jours jusqu'à une date
     * 
     * @param date Date cible
     * @return Nombre de jours jusqu'à cette date
     */
    public static long joursJusqua(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
    
    /**
     * Vérifie si une date est expirée
     * 
     * @param date Date à vérifier
     * @return true si la date est passée
     */
    public static boolean estExpiree(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Vérifie si une date expire bientôt
     * 
     * @param date Date à vérifier
     * @param joursAlerte Nombre de jours d'alerte
     * @return true si la date expire dans moins de joursAlerte jours
     */
    public static boolean expireBientot(LocalDate date, int joursAlerte) {
        if (date == null) {
            return false;
        }
        LocalDate dateAlerte = LocalDate.now().plusDays(joursAlerte);
        return date.isBefore(dateAlerte) && date.isAfter(LocalDate.now());
    }
    
    /**
     * Ajoute des jours à une date
     * 
     * @param date Date de base
     * @param jours Nombre de jours à ajouter
     * @return Nouvelle date
     */
    public static LocalDate ajouterJours(LocalDate date, int jours) {
        if (date == null) {
            return LocalDate.now().plusDays(jours);
        }
        return date.plusDays(jours);
    }
    
    /**
     * Ajoute des mois à une date
     * 
     * @param date Date de base
     * @param mois Nombre de mois à ajouter
     * @return Nouvelle date
     */
    public static LocalDate ajouterMois(LocalDate date, int mois) {
        if (date == null) {
            return LocalDate.now().plusMonths(mois);
        }
        return date.plusMonths(mois);
    }
    
    /**
     * Obtient le premier jour du mois
     * 
     * @param date Date de référence
     * @return Premier jour du mois
     */
    public static LocalDate premierJourDuMois(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return date.withDayOfMonth(1);
    }
    
    /**
     * Obtient le dernier jour du mois
     * 
     * @param date Date de référence
     * @return Dernier jour du mois
     */
    public static LocalDate dernierJourDuMois(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }
    
    /**
     * Vérifie si deux dates sont le même jour
     * 
     * @param date1 Première date
     * @param date2 Deuxième date
     * @return true si c'est le même jour
     */
    public static boolean estMemeJour(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.equals(date2);
    }
    
    /**
     * Vérifie si une date est aujourd'hui
     * 
     * @param date Date à vérifier
     * @return true si c'est aujourd'hui
     */
    public static boolean estAujourdhui(LocalDate date) {
        return estMemeJour(date, LocalDate.now());
    }
    
    /**
     * Vérifie si une date est dans le passé
     * 
     * @param date Date à vérifier
     * @return true si la date est passée
     */
    public static boolean estDansLePasse(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isBefore(LocalDate.now());
    }
    
    /**
     * Vérifie si une date est dans le futur
     * 
     * @param date Date à vérifier
     * @return true si la date est future
     */
    public static boolean estDansLeFutur(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Convertit LocalDateTime en LocalDate
     * 
     * @param dateTime Date-heure à convertir
     * @return LocalDate
     */
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate();
    }
    
    /**
     * Convertit LocalDate en LocalDateTime (début de journée)
     * 
     * @param date Date à convertir
     * @return LocalDateTime à 00:00:00
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }
    
    /**
     * Obtient le nom du jour de la semaine en français
     * 
     * @param date Date
     * @return Nom du jour
     */
    public static String obtenirNomJour(LocalDate date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH);
        return date.format(formatter);
    }
    
    /**
     * Obtient le nom du mois en français
     * 
     * @param date Date
     * @return Nom du mois
     */
    public static String obtenirNomMois(LocalDate date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH);
        return date.format(formatter);
    }
    
    /**
     * Formatte une durée en texte lisible
     * 
     * @param jours Nombre de jours
     * @return Texte formaté (ex: "2 mois et 15 jours")
     */
    public static String formaterDuree(long jours) {
        if (jours == 0) {
            return "0 jour";
        }
        
        if (jours < 0) {
            return "Il y a " + formaterDuree(-jours);
        }
        
        long annees = jours / 365;
        long mois = (jours % 365) / 30;
        long joursRestants = jours % 30;
        
        StringBuilder sb = new StringBuilder();
        
        if (annees > 0) {
            sb.append(annees).append(" an").append(annees > 1 ? "s" : "");
        }
        if (mois > 0) {
            if (sb.length() > 0) sb.append(" et ");
            sb.append(mois).append(" mois");
        }
        if (joursRestants > 0) {
            if (sb.length() > 0) sb.append(" et ");
            sb.append(joursRestants).append(" jour").append(joursRestants > 1 ? "s" : "");
        }
        
        return sb.toString();
    }
}


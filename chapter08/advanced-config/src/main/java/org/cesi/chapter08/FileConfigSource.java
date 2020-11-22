package org.cesi.chapter08;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FileConfigSource implements ConfigSource {

    //emplacement du fichier externe de configuration de l'app
    private final String CONFIG_FILE = "/home/cesi/projects/cloudquarkus/chapter08/config.properties";
   //nom de la source de configuration - utile pour logguer, etc.
    private final String CONFIG_SOURCE_NAME = "externalConfigSource";
    //niveau de priorité de la source de confifuration (+ l'ordinal est elevé, + la source est prioritaire)
    private final int ordinal = 900;
    //obtention de la liste des propriétés sous la forme d'un ensemble de clés / valeurs
    @Override
    public Map<String, String> getProperties() {
        //création d"un flux d'entrée (lecture) lié au fichier externe de config
        try(InputStream in = new FileInputStream(CONFIG_FILE)){
            //création d'une représentation des propriétés
            Properties  properties = new Properties();
            //Instanciation de la map qui va contenir toutes les propriétés sous forme de clé/valeur
            Map<String, String> map = new HashMap<>();
            //chargement des propriétés depuis le flux d'entrée.
            properties.load(in);
            //parcours de la listes des props pour les stocker dans la map.
            // pour cela on récupère la liste des clés (stringPropertyNames qu'on parcourt et on ajoute chaque clé et sa valeur à la Map
            properties.stringPropertyNames().stream().forEach(key->map.put(key,properties.getProperty(key)));

            return map;
           //exception liées à l'ouverture d'une flux d'entrée.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //retourne l'ensemble des clés sans évaluer les valeurs associés.
    @Override
    public Set<String> getPropertyNames() {
        //code semblable à celui de la méthode getProperties (on aurait pu factoriser)
        //création d"un flux d'entrée (lecture) lié au fichier externe de config
        try(InputStream in = new FileInputStream(CONFIG_FILE)){
            //création d'une représentation des propriétés
            Properties  properties = new Properties();
            //chargement des propriétés depuis le flux d'entrée.
            properties.load(in);
            //parcours de la listes des props pour les stocker dans la map.
            // pour cela on récupère la liste des clés (stringPropertyNames qu'on parcourt et on ajoute chaque clé et sa valeur à la Map
           return  properties.stringPropertyNames();
            //exception liées à l'ouverture d'une flux d'entrée.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //comme ordinal =900, cette source de configuration sera prioritaire
    //dans l'ordre de priorité config.properties > propriétés système > var env > application.properties
    @Override
    public int getOrdinal() {
        return ordinal;
    }

    //retourne la valeur associée à une clé
    @Override
    public String getValue(String s) {
        //code semblable à celui de  getPropertyNames (on aurait pu factoriser)
        //création d"un flux d'entrée (lecture) lié au fichier externe de config
        try(InputStream in = new FileInputStream(CONFIG_FILE)){
            //création d'une représentation des propriétés
            Properties  properties = new Properties();
            //chargement des propriétés depuis le flux d'entrée.
            properties.load(in);
            //retour de la valeur de la propriété correspondant à la clé passée en arg.
            return properties.getProperty(s);

            //exception liées à l'ouverture d'une flux d'entrée.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //retour du nom donné à la source de propriétés externe
    @Override
    public String getName() {
        return CONFIG_SOURCE_NAME;
    }
}

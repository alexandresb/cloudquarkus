package org.cesi.chapter08;

import org.eclipse.microprofile.config.spi.Converter;

import java.util.StringTokenizer;

public class CustomConfigPropConverter implements Converter<CustomConfigProperty> {
    //permet de convertir la valeur d'une propriété de config d'appli en une instance d'un type personnalisé
    @Override
    //note :contrairement au livre,  j'ai utilisé ici le tokeniser au lieu de l'utiliser dans CustomConfigProperty
    //car c'est au convertisseur de fragmenter la valeur de la propriété de configuration
    public CustomConfigProperty convert(String s) {
        //on récupère les fragments séparés par un ;
        StringTokenizer st = new StringTokenizer(s,";");
        return new CustomConfigProperty(st.nextToken(), st.nextToken());
    }
}

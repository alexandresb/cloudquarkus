package org.cesi.chapter08;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class ExampleResource {

   // @Inject
    @ConfigProperty(name="service.msg", defaultValue = "hello")
    String msg;

    //utilisation des convertisseurs intégrés de l'API Config d'Eclipse MicroProfile 3.3
    @ConfigProperty(name = "year", defaultValue = "2020")
    int year;
    @ConfigProperty(name = "isUser", defaultValue = "false")
    Boolean isUser;
    @ConfigProperty(name = "students")
    List<String> students;
    @ConfigProperty(name = "pets")
    String[]pets;
    //injection de l'objet représentant l'ensemble de la configuration
    @Inject
    Config config;

    //injection d'une propriété nécessitant un convertisseur customisé
    @ConfigProperty(name = "customConfig")
    CustomConfigProperty customConfigProperty;


    @Path("/hello")
    @GET
    public String hello() {
        return msg;
    }

    @Path("/year")
    @GET
    public Integer year(){
        return year; //type int autoboxé
    }

   @Path("/isuser")
   @GET
   public Boolean isUser(){
     return isUser;
   }

   @Path("/laststudent")
   @GET
   public String getLastStudent(){
     return students.get(students.size()-1);
   }
   //n'est pas dans le livre
   @Path("/students")
   @GET
   public List<String> getAllStudent(){
    return students;
   }

   @Path("/pet/{id}")
   @GET
   public String getPetById(@PathParam("id")Integer id){
    try{
     //rappel un tableau a un indice qui commence à 0
     return pets[id-1];
    }catch(ArrayIndexOutOfBoundsException ex){ //gestion de l'exception non présente dans le livre
     throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

   }

   @Path("/config")
   @GET
   public List<String> getSomeConfigFromdDifferentSources(){
     List<String> props = new ArrayList<>();

      props.add(config.getValue("service.msg",String.class));
      props.add(config.getValue("prop.in.app.properties",String.class));

     return props;

   }

    @Path("/email-user")
    @GET
   public String getUserInfo(){
        return customConfigProperty.getEmail()+"--"+customConfigProperty.getUser();

   }
}
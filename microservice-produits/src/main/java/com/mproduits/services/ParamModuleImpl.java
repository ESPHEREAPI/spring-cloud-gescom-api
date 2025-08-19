/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Property;
import com.mproduits.repositories.PropertyRepositories;
import java.util.ArrayList;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

/**
 *
 * @author USER01
 */
@Data
@Service
public class ParamModuleImpl implements IParamModule{
    PropertyRepositories propertyRepositoies;
     Property prop;

    public ParamModuleImpl(PropertyRepositories propertyRepositoies) {
        this.propertyRepositoies = propertyRepositoies;
        this.prop=new Property();
    }
    

    @Override
    public Property getParamModule() {
      Property p=new Property();
        //je verifie qu'il est une seule ligne
        if(propertyRepositoies.count()==1){
      
               p= (Property) propertyRepositoies.findAll().toArray()[0];
        }
       return p;
    
    }

    @Override
    public void update(Property p) {
    propertyRepositoies.save(p);
    }

    @Override
    public void initProperty() {
     prop=new Property();
       //je cherche si il y'a deja eu un parametrage
        Collection<Property> colProperty=new ArrayList<Property>();
        colProperty=propertyRepositoies.findAll();
        if(colProperty==null || colProperty.isEmpty()==true){
            //je mets les proprietes par defaut
            //module inscription
            prop.setMatriculeAuto(false);
            prop.setEnvoiSmsForDiscipline(Boolean.TRUE);
            prop.setEnvoiSmsForPayement(Boolean.TRUE);
            prop.setEnvoiSmsForDiscipline(Boolean.FALSE);
            //module payement         
            propertyRepositoies.save(prop);
        }
    
    }
    
}

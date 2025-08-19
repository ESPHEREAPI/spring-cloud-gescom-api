/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mproduits.services;

import com.mproduits.model.Property;

/**
 *
 * @author USER01
 */
public interface IParamModule {
    //recuperer toutes les proprietes
    public Property getParamModule();
        
    //modifier une property
    public void update(Property p);
    
    //initialiser le remplissage
      public void initProperty();
}

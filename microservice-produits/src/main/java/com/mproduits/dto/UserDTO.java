/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mproduits.dto;

import java.util.Date;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class UserDTO {

    private Long id;
    private String firstName;
    private String lastname;
    private String userName;
   
    private Boolean isActive;

//    public Boolean getIsActive() {
//        return isActive;
//    }
//
//    public void setIsActive(Boolean isActive) {
//        this.isActive = isActive;
//    }
 


}

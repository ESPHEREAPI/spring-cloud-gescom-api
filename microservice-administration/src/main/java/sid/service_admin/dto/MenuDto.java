/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class MenuDto {
      private Long id;
    private String code;
    private String description;
    private Long moduleId;
}

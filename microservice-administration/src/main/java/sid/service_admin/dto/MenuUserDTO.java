/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sid.service_admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 *
 * @author USER01
 */
@Data
public class MenuUserDTO implements Serializable {

    @JsonProperty("userid")
    private Long userid;

    @JsonProperty("creatBy")
    private String creatBy;

    @JsonProperty("moduleid")
    private Long moduleid;
    @JsonProperty("menus")
    private List<MenuDto> menus;

    public MenuUserDTO() {
    }

}

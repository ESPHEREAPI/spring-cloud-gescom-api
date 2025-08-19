/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package sid.service_admin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 *
 * @author USER01
 */
@Component
@ConfigurationProperties("app")
@RefreshScope
@Data
public class ApplicationPropertiesConfiguration {
 private int limitDeProduits;
 private Long jwtExpirationMs;
}

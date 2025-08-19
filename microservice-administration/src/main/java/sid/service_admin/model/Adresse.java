/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sid.service_admin.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author fabrice
 */
@Embeddable
public class Adresse implements Serializable {
    
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    @Column(name="Ville")
    private String ville;
    
    @Column(name="Tel")
    private String tel;

    @Column(name="Fax")
    private String fax;

    @Column(name="Bp")
    private String bp;
        // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation

    @Column(name="Email")
    private String email;
    
    @Column(name="Quartier")
    private String quartier;
    
    @Column(name="Region")
    private String region;
     
    @Column(name="indicatifPays")
    private String indicatifPays;

    public Adresse() {
        this.ville = "";
        this.tel = "";
        this.fax = "";
        this.bp = "";
        this.email = "";
        this.quartier = "";
        this.region = "";
    }

    public Adresse(String ville, String tel, String fax, String bp, String email, String quartier, String region) {
        this.ville = ville;
        this.tel = tel;
        this.fax = fax;
        this.bp = bp;
        this.email = email;
        this.quartier = quartier;
        this.region = region;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }


    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getIndicatifPays() {
        return indicatifPays;
    }

    public void setIndicatifPays(String indicatifPays) {
        this.indicatifPays = indicatifPays;
    }
    
    
}

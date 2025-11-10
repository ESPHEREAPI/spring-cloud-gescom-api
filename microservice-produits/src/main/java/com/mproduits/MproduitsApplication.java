package com.mproduits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableConfigurationProperties
@EnableDiscoveryClient
@EnableCaching  // Ajouter cette annotation !
public class MproduitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MproduitsApplication.class, args);
    }

//    @Bean
//    CommandLineRunner start(AnneeRepository anneeRepository, EntrepriseRepositories entrepriseRepositories, MoisRepositories moisRepositories, HttpSession session) {
//        return args -> {
//
//            int nombre = IdleDate.getMonth(new Date());
//            int dateCurent = IdleDate.getYear(new Date());
//            Annee an = anneeRepository.findById(dateCurent);
//            if (an == null) {
//                an = new Annee();
//                an.setId(dateCurent);
//                an.setCode("" + dateCurent);
//                an.setLibelle("Annee " + dateCurent);
//                anneeRepository.save(an);
//            }
//            //date = new Date();
//
//            Mois mois = moisRepositories.findOneByAnneeAndNumero(dateCurent, nombre);
//            Entreprise e = entrepriseRepositories.findByActif(Boolean.TRUE);
//            session.setAttribute("mois", mois);
//            session.setAttribute("entreprise", e); // si nécessaire
//
//        };
//    }
}

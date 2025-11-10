package sid.service_admin;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import sid.service_admin.repository.PersonneRepository;
import sid.service_admin.service.InitiationDb;

@SpringBootApplication
@EnableConfigurationProperties
@EnableDiscoveryClient
public class ServiceAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAdminApplication.class, args);
    }

    @Bean
    CommandLineRunner start(InitiationDb initiationDb,PersonneRepository personneRepository) {
       return args -> {

            //creation d un utilisateur admin
            //initiationDb.createPermission();
           // initiationDb.createRoles();
            initiationDb.addIndicatifPays();
            initiationDb.getAllPays();
            initiationDb.getAllReligions();
            initiationDb.getAllTitres();
            initiationDb.getAllModuleSecurite();
            initiationDb.getMenuByModuleFacturation();
           // if (personneRepository.findByUserName("admin").isPresent()==Boolean.FALSE) {
              // initiationDb.getAdmin(); 
           //}
           
        };
    }
    
//    @Bean
//@Primary
//public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//    JpaTransactionManager tm = new JpaTransactionManager();
//    tm.setEntityManagerFactory(emf);
//    tm.setRollbackOnCommitFailure(true);
//    return tm;
//}
}

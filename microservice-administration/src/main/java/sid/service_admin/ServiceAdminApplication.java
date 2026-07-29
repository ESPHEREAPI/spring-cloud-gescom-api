package sid.service_admin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import sid.service_admin.service.InitiationDb;

@SpringBootApplication
@EnableConfigurationProperties
@EnableDiscoveryClient
public class ServiceAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAdminApplication.class, args);
    }

    @Bean
    CommandLineRunner start(InitiationDb initiationDb) {
       return args -> {
            initiationDb.addIndicatifPays();
            initiationDb.getAllPays();
            initiationDb.getAllReligions();
            initiationDb.getAllTitres();
            initiationDb.getAllModuleSecurite();
            initiationDb.getMenuByModuleFacturation();
            initiationDb.getMenuByModulePhotocopie();
            initiationDb.getMenuByModuleStock();
            initiationDb.getMenuByModuleVente();
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

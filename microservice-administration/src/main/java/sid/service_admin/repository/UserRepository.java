///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
// */
//package sid.service_admin.repository;
//
//import feign.Param;
//import java.util.Optional;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//import sid.service_admin.model.User;
//
///**
// *
// * @author USER01
// */
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Long> {
//    Optional<User> findByEmail(String email);
//    Optional<User> findByUserName(String userName);
//    boolean existsByEmail(String email);
//      @Query("SELECT u  FROM User u  WHERE u.userName = :userName and u.password= :password")
//    public User getAuthentification(@Param("userName") String  userName, @Param("password") String  password);
//}
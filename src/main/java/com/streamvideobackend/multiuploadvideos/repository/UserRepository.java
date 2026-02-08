package com.streamvideobackend.multiuploadvideos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.streamvideobackend.multiuploadvideos.dto.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	
	
	@Query("select u from User u where u.email=?1  and u.password=?2 ")
	public User verifyByUser(String email, String pass);
	
	
//	@NativeQuery("UPDATE secondvideobase.user SET name=?1, password=?2, biodetails=?3, country=?4 where id=?5;")
    @Query("UPDATE User u SET u.name = ?1, u.biodetails = ?2, u.country = ?3 WHERE u.id = ?4")
	public User updateDetails(String name, String biodetails, String country, int id);
	
// for password changing 	
	@Query("UPDATE User u SET u.password=?1 where u.id=?2 and u.password=?3")
	public User updateUserPasword(String password, int id, String oldPassword);
	
	

	
//	@Query("select u.name from User u")
//	public List<User> getAllExceptPassword();

}

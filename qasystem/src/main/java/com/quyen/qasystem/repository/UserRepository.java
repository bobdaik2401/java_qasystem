package com.quyen.qasystem.repository;

import com.quyen.qasystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String usename);

    Optional<User> findByEmail(String email);

    // dùng cho đăng ký
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    // login bằng email OR username
    // dùng cho login: email OR username (ignore case)
    @Query("""
        select u from User u
        where lower(u.username) = lower(:identifier)
           or lower(u.email) = lower(:identifier)
    """)
    Optional<User> findForLogin(@Param("identifier") String identifier);

}

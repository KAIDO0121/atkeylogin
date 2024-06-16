package ATKeyLogin.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ATKeyLogin.backend.model.AlternativeEmail;
import jakarta.transaction.Transactional;

import java.util.*;

@Transactional
@Repository
public interface AlternativeEmailDAO extends JpaRepository<AlternativeEmail, Long>{
    public List<AlternativeEmail> findByUserId(Long userId);

    @Modifying
    @Query(nativeQuery = true, value = "insert into alternative_emails (email,user_id) values (?1,?2) ON CONFLICT DO NOTHING ")
    void insertAlternativeEmail(String alternativeEmail, Long userId);
}

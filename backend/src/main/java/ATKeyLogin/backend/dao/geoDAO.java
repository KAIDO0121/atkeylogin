package ATKeyLogin.backend.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

import org.springframework.stereotype.Repository;

import ATKeyLogin.backend.model.Device;

@Repository
public interface geoDAO extends JpaRepository<Device, Long>{
    
}

package ATKeyLogin.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ATKeyLogin.backend.model.OEM;

public interface OEMDAO extends JpaRepository<OEM, Long> {
    OEM findByOEMId(String OEMId);
}

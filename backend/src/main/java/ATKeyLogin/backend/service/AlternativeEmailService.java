package ATKeyLogin.backend.service;
import org.springframework.stereotype.Service;

import ATKeyLogin.backend.dao.AlternativeEmailDAO;
import ATKeyLogin.backend.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class AlternativeEmailService {

    private AlternativeEmailDAO alternativeEmailRepository;

    public AlternativeEmailService(
        AlternativeEmailDAO alternativeEmailRepository
    ) {
        this.alternativeEmailRepository = alternativeEmailRepository;
    }

    public List<AlternativeEmail> findByUserId(Long userId) {
        return alternativeEmailRepository.findByUserId(userId);
    }

    public void createEmail(AlternativeEmail alternativeEmail) {

        alternativeEmailRepository.insertAlternativeEmail(alternativeEmail.getEmail(), alternativeEmail.getUser().getId());
    }
}

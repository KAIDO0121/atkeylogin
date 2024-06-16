package ATKeyLogin.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ATKeyLogin.backend.domain.TempDTO;
import ATKeyLogin.backend.domain.TempVeriCodeDTO;
import ATKeyLogin.backend.domain.TokenRefreshReqDTO;
import ATKeyLogin.backend.domain.Attestation.Options.AttestationParamsDTO;
import ATKeyLogin.backend.domain.Attestation.Results.AttestationRequestDTO;
import ATKeyLogin.backend.domain.Response.ResponseHandler;
import ATKeyLogin.backend.model.RefreshToken;
import ATKeyLogin.backend.service.UserService;
import ATKeyLogin.backend.service.AuthFiService;
import ATKeyLogin.backend.service.JWTService;
import ATKeyLogin.backend.service.UserDetailsImpl;
import ATKeyLogin.backend.service.RefreshTokenService;
import ATKeyLogin.backend.model.User;
import ATKeyLogin.backend.model.exception.AuthFiServiceException;



@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api")
@RestController
public class AttestationController {

    private static final Logger log = LoggerFactory.getLogger(AttestationController.class);

    // @GetMapping("/employees")
    // List<Employee> all() {
    //     return repository.findAll();
    // }

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthFiService authFiService;

    @Autowired
    private JWTService jwtUtils;  

    @PostMapping(path = "/attestation/userInfo" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> newUserTemp(@Valid @RequestBody TempDTO TempDTO) throws IOException{
        Map<String, Object> payload = new HashMap<>();
        String addedTempEmail = userService.addTempUser(TempDTO);
        payload.put("signupEmail", addedTempEmail);
        return ResponseHandler.getSuccessRes(payload);
    }

    @PostMapping(path = "/attestation/verification-code" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> checkVeriCode(@Valid @RequestBody TempVeriCodeDTO TempVeriCodeDTO) throws Exception{
        Map<String, Object> m = new HashMap<>();
        User u = userService.validateTemp(TempVeriCodeDTO);
        
        Authentication authentication = authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(TempVeriCodeDTO.getEmail(), TempVeriCodeDTO.getEmail()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        String jwt = jwtUtils.generateJwtToken(userDetails);
        
        // ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), userDetails.getEmail());
        
        m.put("authorities", userDetails.getAuthorities().iterator().next().getAuthority());
        m.put("refreshToken", (String)refreshToken.getToken());
        m.put("accessToken", jwt);
        m.put("userId", u.getId());

        return ResponseHandler.getSuccessRes(m);
    }

    @PostMapping("/attestation/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshReqDTO TokenRefreshReqDTO) {
        Map<String, Object> m = new HashMap<>();

        String requestRefreshToken = TokenRefreshReqDTO.getRefreshToken();
        
        RefreshToken t = refreshTokenService.findByToken(requestRefreshToken);
        
        refreshTokenService.verifyExpiration(t.getToken());
        
        String jwt = jwtUtils.generateTokenFromUserEmail(t.getEmail());
        
        m.put("refreshToken", requestRefreshToken);
        m.put("accessToken", jwt);

        return ResponseHandler.getSuccessRes(m);
    }

    @PostMapping(path = "/attestation/options" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getOptionsForSignUp(@Valid @RequestBody AttestationParamsDTO AttestationParamsDTO) throws AuthFiServiceException{
        AttestationParamsDTO.getParams().getAuthenticatorSelection().setUserVerification("required");

        Map res = authFiService.attestationOpt(AttestationParamsDTO);
        return ResponseHandler.getSuccessRes(res);
    }

    @PostMapping(path = "/attestation/result" , produces = "application/json; charset=UTF-8")
    public ResponseEntity<Map> getResultForSignUp(@Valid @RequestBody AttestationRequestDTO AttestationRequestDTO) throws AuthFiServiceException{
        Map res = authFiService.attestationRes(AttestationRequestDTO);
        
        return ResponseHandler.getSuccessRes(res);
    }
}


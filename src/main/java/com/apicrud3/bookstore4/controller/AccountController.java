package com.apicrud3.bookstore4.controller;

import com.apicrud3.bookstore4.model.AppUser;
import com.apicrud3.bookstore4.model.RegisterDto;
import com.apicrud3.bookstore4.repository.AppRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.plaf.PanelUI;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/account")
public class AccountController {

@Value("${spring.jwt.secret-key}")
private String jwtSecretKey;

@Value("${spring.jwt.issuer}")
private String jwtIssuer;

@Autowired
private AppRepository appRepository;
@PostMapping("/register")
public ResponseEntity<Object> register(
        @Valid @RequestBody RegisterDto registerDto,
        BindingResult result) {

    if (result.hasErrors()) {
        var errorList = result.getAllErrors();
        var errorsMap = new HashMap<String, String>();

        for (int i = 0; i < errorList.size(); i++) {
            var error = (FieldError) errorList.get(i);
            errorsMap.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errorsMap);
    }
    var bCryptEncoder = new BCryptPasswordEncoder();
    AppUser appUser = new AppUser();
    appUser.setFirstName(registerDto.getFirstName());
    appUser.setLastName(registerDto.getLastName());
    appUser.setUsername(registerDto.getUsername());
    appUser.setEmail(registerDto.getEmail());
    appUser.setRole("USER");
    appUser.setCreated_at(new Date());
    appUser.setPassword(bCryptEncoder.encode(registerDto.getPassword()));


}


private String createJwtToken(AppUser appUser) {
    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtIssuer)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(24 * 3600))
            .subject(appUser.getUsername())
            .claim("role", appUser.getRole())
            .build();
    var encoder = new NimbusJwtEncoder(
            new ImmutableSecret<>(jwtSecretKey.getBytes()));
    var params = JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256) .build(), claims

    );
    return encoder.encode(params) .getTokenValue();

}

}

package com.apicrud3.bookstore4.controller;

import com.apicrud3.bookstore4.model.AppUser;
import com.apicrud3.bookstore4.model.LoginDto;
import com.apicrud3.bookstore4.model.RegisterDto;
import com.apicrud3.bookstore4.repository.AppRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Value("${security.jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/profile")
    public ResponseEntity<Object> profile(Authentication auth) {
        var response = new HashMap<String, Object>();
        response.put("UserName", auth.getName());
        response.put("authorities", auth.getAuthorities());

        var appUser = appRepository.findByUsername(auth.getName());
        response.put("user", appUser);

        return ResponseEntity.ok(response);
    }

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

        try {
            //check if user already exists or not
            var otherUser = appRepository.findByUsername(appUser.getUsername());
            if (otherUser != null) {
                return ResponseEntity.badRequest().body("User already exists");
            }
            otherUser = appRepository.findByEmail(appUser.getEmail());
            if (otherUser != null) {
                return ResponseEntity.badRequest().body("Email already exists");
            }
            appRepository.save(appUser);

            String jwtToken = createJwtToken(appUser);
            var response = new HashMap<String, Object>();
            response.put("token", jwtToken);
            response.put("user", appUser);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            System.out.println("Theres is an Exception:");
            ex.printStackTrace();
        }
        return ResponseEntity.badRequest().body("An error occurred");
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDto loginDto, BindingResult result) {
        if (result.hasErrors()) {
            var errorList = result.getAllErrors();
            var errorsMap = new HashMap<String, String>();

            for (int i = 0; i < errorList.size(); i++) {
                var error = (FieldError) errorList.get(i);
                errorsMap.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(errorsMap);
        }
        var appUser = appRepository.findByUsername(loginDto.getUsername());
        if (appUser == null) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken
                            (loginDto.getUsername(),
                                    loginDto.getPassword()));

        AppUser appUser1 = appRepository.findByUsername(loginDto.getUsername());
        String jwtToken = createJwtToken(appUser);
        var response = new HashMap<String, Object>();
        response.put("token", jwtToken);
        response.put("user", appUser);
        return ResponseEntity.ok(response);
        }
        catch (Exception ex) {
            System.out.println("There is an Exception:");
            ex.printStackTrace();

        }
        return ResponseEntity.badRequest().body("Invalid username or password");
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

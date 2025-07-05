    package com.pm.authservice.controller;


    import com.pm.authservice.dto.LoginRequestDTO;
    import com.pm.authservice.dto.LoginResponseDTO;
    import com.pm.authservice.dto.RegisterRequestDTO;
    import com.pm.authservice.dto.RegisterResponseDTO;
    import com.pm.authservice.service.AuthService;
    import io.swagger.v3.oas.annotations.Operation;
    import java.util.Optional;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestHeader;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
            this.authService = authService;
        }

        @Operation(summary = "Generate token on user login")
        @PostMapping("/login")
        public ResponseEntity<LoginResponseDTO> login(
                @RequestBody LoginRequestDTO loginRequestDTO) {

            Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

            if (tokenOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = tokenOptional.get();
            return ResponseEntity.ok(new LoginResponseDTO(token));
        }

        @Operation(summary = "Register new user")
        @PostMapping("/register")
        public ResponseEntity<RegisterResponseDTO> register(
                @RequestBody RegisterRequestDTO registerRequestDTO) {

            Optional<com.pm.authservice.model.User> userOptional = authService.register(registerRequestDTO);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RegisterResponseDTO("User with this email already exists", null));
            }

            com.pm.authservice.model.User user = userOptional.get();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponseDTO("User registered successfully", user.getId()));
        }

        @Operation(summary = "Validate Token")
        @GetMapping("/validate")
        public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String token) {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return authService.validateToken(token.substring(7))
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }

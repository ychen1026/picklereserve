package edu.sjsu.cmpe172.picklereserve.controller;

import edu.sjsu.cmpe172.picklereserve.dto.AuthResponse;
import edu.sjsu.cmpe172.picklereserve.dto.LoginRequest;
import edu.sjsu.cmpe172.picklereserve.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth){this.auth=auth;}
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request,HttpSession session){
        var u=auth.login(request.username(),request.password(),session); return new AuthResponse(u.userId(),u.username(),u.fullName(),u.role());
    }
    @GetMapping("/me") public AuthResponse me(HttpSession session){ var u=auth.requireUser(session); return new AuthResponse(u.userId(),u.username(),u.fullName(),u.role()); }
    @PostMapping("/logout") public void logout(HttpSession session){session.invalidate();}
}

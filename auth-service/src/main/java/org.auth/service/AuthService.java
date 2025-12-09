package org.auth.service;

import lombok.RequiredArgsConstructor;
import org.auth.dto.LoginRequest;
import org.auth.dto.RegisterRequest;
import org.auth.model.Role;
import org.auth.model.Tenant;
import org.auth.model.User;
import org.auth.repository.RoleRepository;
import org.auth.repository.TenantRepository;
import org.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public void registerUser(RegisterRequest registerRequest) {
        Tenant tenant = new Tenant(null, registerRequest.getTenantName());
        tenantRepository.save(tenant);
        Role role =  roleRepository.findById(1L)
                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setTenant(tenant);
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public String login(LoginRequest req){
        User u = userRepository.findByEmail(req.getEmail()).orElseThrow(() -> new RuntimeException("Invalid Credentials"));
        if(!passwordEncoder.matches(req.getPassword(),u.getPassword())){
           throw new RuntimeException("Invalid Credentials");
        }
        return jwtService.generateToken(u.getEmail(),u.getTenant().getId());
    }
}

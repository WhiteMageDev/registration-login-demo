package com.chat.webchat.appuser;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.chat.webchat.registration.token.ConfirmationToken;
import com.chat.webchat.registration.token.ConfirmationTokenService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final AppUserRepo appUserRepository;
    private final BCryptPasswordEncoder encoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format("User with email [%s] is not found", email)));
    }

    public boolean emailExists(String email) {
        return appUserRepository.findByEmail(email).isPresent();
    }

    public boolean usernameExists(String username) {
        return appUserRepository.findByUsername(username).isPresent();
    }

    public String signUpUser(AppUser appUser) {

        String encoded = encoder.encode(appUser.getPassword());
        appUser.setPassword(encoded);
        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser);

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return token;
    }

    public int enableAppUser(String username) {
        return appUserRepository.enableAppUser(username);
    }

}

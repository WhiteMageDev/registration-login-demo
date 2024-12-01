package com.chat.webchat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import com.chat.webchat.appuser.AppUser;
import com.chat.webchat.appuser.AppUserRole;
import com.chat.webchat.appuser.AppUserService;
import com.chat.webchat.email.EmailService;
import com.chat.webchat.registration.RegistrationRequest;
import com.chat.webchat.registration.RegistrationService;
import com.chat.webchat.registration.token.ConfirmationToken;
import com.chat.webchat.registration.token.ConfirmationTokenService;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private EmailService emailSender;

    @Mock
    private ConfirmationTokenService confirmationTokenService;

    private RegistrationService registrationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        registrationService = new RegistrationService(appUserService, confirmationTokenService, emailSender);
    }

    @Test
    public void testRegisterWhenEmailExists() {

        String email = "user@example.com";
        String username = "user123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        when(appUserService.emailExists(anyString())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(request);
        });
        assertTrue(appUserService.emailExists(email));
        assertEquals("Email [user@example.com] has already been taken.", exception.getMessage());
    }

    @Test
    public void testRegisterWhenUsernameExists() {
        String email = "newuser@example.com";
        String username = "user123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        when(appUserService.emailExists(email)).thenReturn(false);
        when(appUserService.usernameExists(username)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(request);
        });

        assertEquals("Username [user123] has already been taken.", exception.getMessage());
    }

    @Test
    public void testRegisterSuccess() {
        String email = "newuser@example.com";
        String username = "newuser123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);
        String token = "generatedToken";

        when(appUserService.emailExists(email)).thenReturn(false);
        when(appUserService.usernameExists(username)).thenReturn(false);
        when(appUserService.signUpUser(any(AppUser.class))).thenReturn(token);

        String result = registrationService.register(request);

        assertEquals(token, result);
        verify(emailSender, times(1)).send(eq(email), anyString());
    }

    @Test
    public void testRegisterWhenUsernameExistsAfterEmailCheck() {
        String email = "newuser@example.com";
        String username = "user123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        when(appUserService.emailExists(email)).thenReturn(false);
        when(appUserService.usernameExists(username)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.register(request);
        });

        assertEquals("Username [user123] has already been taken.", exception.getMessage());
    }

    @Test
    public void testRegisterWithEmptyEmail() {
        String email = "";
        String username = "user123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register(request);
        });

        // It is assumed that form validation occurs on the frontend.

        assertEquals("Not implemented exception.", exception.getMessage());
    }

    @Test
    public void testRegisterWithEmptyUsername() {
        String email = "newuser@example.com";
        String username = "";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register(request);
        });

        // It is assumed that form validation occurs on the frontend.

        assertEquals("Not implemented exception.", exception.getMessage());
    }

    @Test
    public void testRegisterWithEmptyPassword() {
        String email = "newuser@example.com";
        String username = "newuser123";
        String password = "";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register(request);
        });

        // It is assumed that form validation occurs on the frontend.

        assertEquals("Not implemented exception.", exception.getMessage());
    }

    @Test
    public void testRegisterWithInvalidEmail() {
        String email = "invalid-email";
        String username = "newuser123";
        String password = "password123";
        RegistrationRequest request = new RegistrationRequest(username, email, password);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register(request);
        });

        // It is assumed that form validation occurs on the frontend.

        assertEquals("Not implemented exception.", exception.getMessage());
    }

    @Test
    public void testRegisterWithNullValues() {
        RegistrationRequest request = new RegistrationRequest(null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registrationService.register(request);
        });

        // It is assumed that form validation occurs on the frontend.
        
        assertEquals("Not implemented exception.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenTokenNotFound() {
        String token = "invalidToken";

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Token not found.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenEmailAlreadyConfirmed() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmed(LocalDateTime.now());

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Email address already confirmed.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenTokenExpired() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setExpires(LocalDateTime.now().minusMinutes(1));

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Token expired.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenSuccessful() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setConfirmed(null);
        confirmationToken.setExpires(LocalDateTime.now().plusMinutes(10));
        AppUser appUser = new AppUser();
        appUser.setUsername("user123");
        confirmationToken.setAppUser(appUser);

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String result = registrationService.confirmToken(token);

        assertEquals("Token confirmed successfully.", result);
        verify(confirmationTokenService).setConfirmed(token);
        verify(appUserService).enableAppUser(appUser.getUsername());
    }

    @Test
    public void testConfirmTokenWhenTokenIsNull() {
        String token = null;

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Token not found.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenTokenIsEmpty() {
        String token = "";

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Token not found.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenTokenExpiresExactlyNow() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        confirmationToken.setExpires(LocalDateTime.now());

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registrationService.confirmToken(token);
        });

        assertEquals("Token expired.", exception.getMessage());
    }

    @Test
    public void testConfirmTokenWhenTokenExpiresInTheFuture() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        AppUser user = new AppUser("username", "user@test.com", "password123", AppUserRole.USER);
        confirmationToken.setAppUser(user);
        confirmationToken.setExpires(LocalDateTime.now().plusSeconds(1));

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String result = registrationService.confirmToken(token);

        assertEquals("Token confirmed successfully.", result);
        verify(confirmationTokenService).setConfirmed(token);
        verify(appUserService).enableAppUser(confirmationToken.getAppUser().getUsername());
    }

    @Test
    public void testConfirmTokenWhenTokenExpiresFarInTheFuture() {
        String token = "validToken";
        ConfirmationToken confirmationToken = new ConfirmationToken();
        AppUser user = new AppUser("username", "user@test.com", "password123", AppUserRole.USER);
        confirmationToken.setAppUser(user);
        confirmationToken.setExpires(LocalDateTime.now().plusYears(1));

        when(confirmationTokenService.getToken(token)).thenReturn(Optional.of(confirmationToken));

        String result = registrationService.confirmToken(token);

        // The token has a lifetime of 15 minutes and cannot expire in the far future.
        assertEquals("Not implemented exception.", result);
        verify(confirmationTokenService).setConfirmed(token);
        verify(appUserService).enableAppUser(confirmationToken.getAppUser().getUsername());
    }
}

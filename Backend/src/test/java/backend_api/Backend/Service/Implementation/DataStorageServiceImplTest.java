package backend_api.Backend.Service.Implementation;

import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Repository.SolicitudDataRepository;
import backend_api.Backend.Repository.UserDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataStorageServiceImpl
 * Testing user data storage, deactivation, and retrieval operations
 */
@ExtendWith(MockitoExtension.class)
class DataStorageServiceImplTest {

    @Mock
    private UserDataRepository userDataRepository;

    @Mock
    private ProviderDataRepository providerDataRepository;

    @Mock
    private SolicitudDataRepository solicitudDataRepository;

    @InjectMocks
    private DataStorageServiceImpl dataStorageService;

    private UserData testUserData;
    private Map<String, Object> userDataMap;

    @BeforeEach
    void setUp() {
        testUserData = new UserData();
        testUserData.setId(1L);
        testUserData.setUserId(12345L);
        testUserData.setName("Juan Pérez");
        testUserData.setFirstName("Juan");
        testUserData.setLastName("Pérez");
        testUserData.setEmail("juan@example.com");
        testUserData.setPhone("+5491123456789");
        testUserData.setDni("12345678");
        testUserData.setRole("CLIENTE");
        testUserData.setActive(true);
        testUserData.setSaldoDisponible(BigDecimal.valueOf(25000.00));

        userDataMap = new HashMap<>();
        userDataMap.put("name", "Juan Pérez");
        userDataMap.put("firstName", "Juan");
        userDataMap.put("lastName", "Pérez");
        userDataMap.put("email", "juan@example.com");
        userDataMap.put("phone", "+5491123456789");
        userDataMap.put("dni", "12345678");
        userDataMap.put("role", "CLIENTE");
        userDataMap.put("active", true);
    }

    @Test
    void testDeactivateUser_Success() {
        // Given
        Long userId = 12345L;
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByUserId(userId)).thenReturn(true);
        when(userDataRepository.deactivateByUserId(userId)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUser(userId, reason));

        // Then
        verify(userDataRepository, times(1)).existsByUserId(userId);
        verify(userDataRepository, times(1)).deactivateByUserId(userId);
    }

    @Test
    void testDeactivateUser_UserNotFound() {
        // Given
        Long userId = 12345L;
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByUserId(userId)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUser(userId, reason));

        // Then
        verify(userDataRepository, times(1)).existsByUserId(userId);
        verify(userDataRepository, never()).deactivateByUserId(anyLong());
    }

    @Test
    void testDeactivateUser_UpdateReturnsZero() {
        // Given
        Long userId = 12345L;
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByUserId(userId)).thenReturn(true);
        when(userDataRepository.deactivateByUserId(userId)).thenReturn(0);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUser(userId, reason));

        // Then
        verify(userDataRepository, times(1)).existsByUserId(userId);
        verify(userDataRepository, times(1)).deactivateByUserId(userId);
    }

    @Test
    void testDeactivateUserByEmail_Success() {
        // Given
        String email = "juan@example.com";
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByEmail(email)).thenReturn(true);
        when(userDataRepository.deactivateByEmail(email)).thenReturn(1);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUserByEmail(email, reason));

        // Then
        verify(userDataRepository, times(1)).existsByEmail(email);
        verify(userDataRepository, times(1)).deactivateByEmail(email);
    }

    @Test
    void testDeactivateUserByEmail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByEmail(email)).thenReturn(false);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUserByEmail(email, reason));

        // Then
        verify(userDataRepository, times(1)).existsByEmail(email);
        verify(userDataRepository, never()).deactivateByEmail(anyString());
    }

    @Test
    void testDeactivateUserByEmail_UpdateReturnsZero() {
        // Given
        String email = "juan@example.com";
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByEmail(email)).thenReturn(true);
        when(userDataRepository.deactivateByEmail(email)).thenReturn(0);

        // When
        assertDoesNotThrow(() -> dataStorageService.deactivateUserByEmail(email, reason));

        // Then
        verify(userDataRepository, times(1)).existsByEmail(email);
        verify(userDataRepository, times(1)).deactivateByEmail(email);
    }

    @Test
    void testDeactivateUser_Exception() {
        // Given
        Long userId = 12345L;
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByUserId(userId)).thenReturn(true);
        when(userDataRepository.deactivateByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataStorageService.deactivateUser(userId, reason));
    }

    @Test
    void testDeactivateUserByEmail_Exception() {
        // Given
        String email = "juan@example.com";
        String reason = "Usuario dado de baja";
        when(userDataRepository.existsByEmail(email)).thenReturn(true);
        when(userDataRepository.deactivateByEmail(email)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> dataStorageService.deactivateUserByEmail(email, reason));
    }

    @Test
    void testSaveUserData_WithNewFields() {
        // Given
        Long userId = 12345L;
        String secondaryId = "msg-123";
        userDataMap.put("firstName", "Juan");
        userDataMap.put("lastName", "Pérez");
        userDataMap.put("dni", "12345678");
        userDataMap.put("active", true);
        
        List<Map<String, Object>> addressList = new ArrayList<>();
        Map<String, Object> address = new HashMap<>();
        address.put("state", "Buenos Aires");
        address.put("city", "CABA");
        address.put("street", "Av. Corrientes");
        address.put("number", "1234");
        address.put("floor", "5");
        address.put("apartment", "A");
        addressList.add(address);
        userDataMap.put("address", addressList);
        
        List<String> zones = Arrays.asList("Zona Norte", "Zona Sur");
        userDataMap.put("zones", zones);
        
        List<String> skills = Arrays.asList("Plomería", "Electricidad");
        userDataMap.put("skills", skills);
        
        userDataMap.put("saldoDisponible", BigDecimal.valueOf(30000.00));

        when(userDataRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userDataRepository.save(any(UserData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> dataStorageService.saveUserData(userId, userDataMap, secondaryId));

        // Then
        verify(userDataRepository, times(1)).findByUserId(userId);
        verify(userDataRepository, times(1)).save(any(UserData.class));
    }

    @Test
    void testSaveUserData_UpdateExistingUser() {
        // Given
        Long userId = 12345L;
        String secondaryId = "msg-456";
        userDataMap.put("active", false);
        userDataMap.put("status", "DEACTIVATED");

        when(userDataRepository.findByUserId(userId)).thenReturn(Optional.of(testUserData));
        when(userDataRepository.save(any(UserData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        assertDoesNotThrow(() -> dataStorageService.saveUserData(userId, userDataMap, secondaryId));

        // Then
        verify(userDataRepository, times(1)).findByUserId(userId);
        verify(userDataRepository, times(1)).save(any(UserData.class));
        assertFalse(testUserData.getActive());
    }
}


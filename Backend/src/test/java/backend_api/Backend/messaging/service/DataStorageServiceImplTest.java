package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Entity.UserData;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.Repository.SolicitudDataRepository;
import backend_api.Backend.Repository.UserDataRepository;
import backend_api.Backend.Service.Implementation.DataStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataStorageServiceImplTest {

    @Mock private UserDataRepository userDataRepository;
    @Mock private ProviderDataRepository providerDataRepository;
    @Mock private SolicitudDataRepository solicitudDataRepository;

    private DataStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DataStorageServiceImpl(userDataRepository, providerDataRepository, solicitudDataRepository);
    }

    @Test
    void saveUserData_creaCliente_conSaldoYDireccion() {
        // no existe -> crea
        when(userDataRepository.findByUserId(10L)).thenReturn(Optional.empty());

        Map<String,Object> map = new HashMap<>();
        map.put("name","Juan G");
        map.put("firstName","Juan");
        map.put("lastName","G");
        map.put("email","j@x.com");
        map.put("phone","123");
        map.put("role","CLIENTE");
        map.put("dni","2211");
        map.put("active", true);
        map.put("saldoDisponible", BigDecimal.valueOf(123.45));
        map.put("address", List.of(Map.of(
                "state","CABA",
                "city","BA",
                "street","San Martín",
                "number","100",
                "floor","2",
                "apartment","A"
        )));
        map.put("zones", List.of("CABA","GBA Sur"));
        map.put("skills", List.of("plomería","electricidad"));

        ArgumentCaptor<UserData> cap = ArgumentCaptor.forClass(UserData.class);

        service.saveUserData(10L, map, "evt-1");

        verify(userDataRepository).save(cap.capture());
        UserData ud = cap.getValue();

        assertEquals(10L, ud.getUserId());
        assertEquals("Juan", ud.getFirstName());
        assertEquals("G", ud.getLastName());
        assertEquals("j@x.com", ud.getEmail());
        assertEquals("CABA", ud.getState());
        assertEquals("BA", ud.getCity());
        assertEquals("San Martín", ud.getStreet());
        assertEquals("100", ud.getNumber());
        assertEquals("2", ud.getFloor());
        assertEquals("A", ud.getApartment());
        assertEquals(BigDecimal.valueOf(123.45), ud.getSaldoDisponible());
        assertEquals(List.of("CABA","GBA Sur"), ud.getZones());
        assertEquals(List.of("plomería","electricidad"), ud.getSkills());
        assertTrue(ud.getActive());
        assertEquals("evt-1", ud.getSecondaryId()); // se guarda en secondaryId el messageId
    }

    @Test
    void saveUserData_actualizaExistente_yRechazoApagaActive() {
        UserData existing = new UserData();
        existing.setUserId(20L);
        existing.setActive(true);

        when(userDataRepository.findByUserId(20L)).thenReturn(Optional.of(existing));

        Map<String,Object> map = new HashMap<>();
        map.put("status","REJECTED");
        map.put("rejectionReason","faltan datos");
        map.put("email","nuevo@mail.com"); // también prueba actualización de campos

        ArgumentCaptor<UserData> cap = ArgumentCaptor.forClass(UserData.class);

        service.saveUserData(20L, map, null);

        verify(userDataRepository).save(cap.capture());
        UserData saved = cap.getValue();
        assertFalse(saved.getActive());
        assertEquals("nuevo@mail.com", saved.getEmail());
    }

    @Test
    void saveProviderData_upsertPorProviderId_yReemplazaCollections() {
        ProviderData existing = new ProviderData();
        existing.setId(99L);
        existing.setProviderId(123L);
        existing.getZones().addAll(List.of("OLD1","OLD2"));
        existing.getSkills().addAll(List.of("OLD-A"));

        when(providerDataRepository.findByProviderId(123L)).thenReturn(Optional.of(existing));

        Map<String,Object> m = new HashMap<>();
        m.put("name","Ana P");
        m.put("email","p@d.com");
        m.put("phone","111");
        m.put("address", List.of(Map.of("state","CABA","city","BA","street","S","number","9")));
        m.put("zones", List.of("CABA","GBA Sur"));
        m.put("skills", List.of("plomero"));

        ArgumentCaptor<ProviderData> cap = ArgumentCaptor.forClass(ProviderData.class);

        service.saveProviderData(123L, m, "30111222");

        verify(providerDataRepository).save(cap.capture());
        ProviderData pd = cap.getValue();

        assertEquals(123L, pd.getProviderId());
        assertEquals("Ana P", pd.getName());
        assertEquals("p@d.com", pd.getEmail());
        assertEquals("111", pd.getPhone());
        assertEquals("CABA", pd.getState());
        assertEquals("BA", pd.getCity());
        assertEquals("S", pd.getStreet());
        assertEquals("9", pd.getNumber());
        assertEquals(List.of("CABA","GBA Sur"), pd.getZones());
        assertEquals(List.of("plomero"), pd.getSkills());
        assertEquals("30111222", pd.getSecondaryId());
    }

    @Test
    void saveProviderData_creaNuevoCuandoNoExiste() {
        when(providerDataRepository.findByProviderId(777L)).thenReturn(Optional.empty());
        when(providerDataRepository.findByEmail("nuevo@p.com")).thenReturn(Optional.empty());

        Map<String,Object> m = new HashMap<>();
        m.put("name","Nuevo P");
        m.put("email","nuevo@p.com");
        m.put("phone","555");
        m.put("zones", List.of("Norte"));
        m.put("skills", List.of("gasista"));

        ArgumentCaptor<ProviderData> cap = ArgumentCaptor.forClass(ProviderData.class);

        service.saveProviderData(777L, m, null);

        verify(providerDataRepository).save(cap.capture());
        ProviderData pd = cap.getValue();
        assertEquals(777L, pd.getProviderId());
        assertEquals(List.of("Norte"), pd.getZones());
        assertEquals(List.of("gasista"), pd.getSkills());
    }

    @Test
    void deactivateUser_actualizaCuandoExiste() {
        when(userDataRepository.existsByUserId(7L)).thenReturn(true);
        when(userDataRepository.deactivateByUserId(7L)).thenReturn(1);

        service.deactivateUser(7L, "baja");

        verify(userDataRepository).deactivateByUserId(7L);
    }

    @Test
    void deactivateUser_noHaceNadaSiNoExiste() {
        when(userDataRepository.existsByUserId(8L)).thenReturn(false);

        service.deactivateUser(8L, "baja");

        verify(userDataRepository, never()).deactivateByUserId(anyLong());
    }

    @Test
    void deactivateUserByEmail_actualizaCuandoExiste() {
        when(userDataRepository.existsByEmail("a@b.com")).thenReturn(true);
        when(userDataRepository.deactivateByEmail("a@b.com")).thenReturn(1);

        service.deactivateUserByEmail("a@b.com", "baja");

        verify(userDataRepository).deactivateByEmail("a@b.com");
    }
}
package backend_api.Backend.messaging.service;

import backend_api.Backend.Entity.ProviderData;
import backend_api.Backend.Repository.ProviderDataRepository;
import backend_api.Backend.messaging.dto.CoreEventMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static java.util.Map.entry;   // <-- para Map.entry(...)
// requiere JDK 9+ (vos usás 17)

class ProviderEventProcessorServiceTest {

    @Test
    void procesa_alta_con_datos_varios_y_activo_false() {
        ProviderDataRepository repo = mock(ProviderDataRepository.class);
        when(repo.findByProviderId(1L)).thenReturn(Optional.empty());

        ProviderEventProcessorService svc = new ProviderEventProcessorService(repo);

        CoreEventMessage m = new CoreEventMessage();
        CoreEventMessage.Destination d = new CoreEventMessage.Destination();
        d.setChannel("catalogue.prestador.alta");
        m.setDestination(d);

        // Map.ofEntries para más de 10 pares
        m.setPayload(Map.ofEntries(
                entry("id", 1),
                entry("nombre", "Ana"),
                entry("apellido", "Pérez"),
                entry("email", "ana@test.com"),
                entry("telefono", "123"),
                entry("dni", "12345678"),
                entry("foto", "url"),
                entry("estado", "BA"),
                entry("ciudad", "CABA"),
                entry("calle", "Siempre Viva"),
                entry("numero", "742"),
                entry("piso", "1"),
                entry("departamento", "A"),
                entry("activo", "false"), // rama boolean flexible
                entry("habilidades", List.of(Map.of("id", 46, "nombre", "Programador Java"))),
                entry("zonas", "Norte, Sur") // rama parseo por comas
        ));

        svc.processProviderFromCore(m);

        ArgumentCaptor<ProviderData> cap = ArgumentCaptor.forClass(ProviderData.class);
        verify(repo).save(cap.capture());
        ProviderData pd = cap.getValue();

        assertThat(pd.getProviderId()).isEqualTo(1L);
        assertThat(pd.getName()).isEqualTo("Ana Pérez");
        assertThat(pd.getEmail()).isEqualTo("ana@test.com");
        assertThat(pd.getActive()).isFalse();
        assertThat(pd.getSkills()).containsExactly("Programador Java");
        assertThat(pd.getZones()).containsExactlyInAnyOrder("Norte", "Sur");
    }

    @Test
    void procesa_modificacion_sobre_existente() {
        ProviderData existente = new ProviderData();
        existente.setId(99L);
        existente.setProviderId(2L);

        ProviderDataRepository repo = mock(ProviderDataRepository.class);
        when(repo.findByProviderId(2L)).thenReturn(Optional.of(existente));

        ProviderEventProcessorService svc = new ProviderEventProcessorService(repo);

        CoreEventMessage m = new CoreEventMessage();
        CoreEventMessage.Destination d = new CoreEventMessage.Destination();
        d.setEventName("modificacion_prestador");
        m.setDestination(d);
        m.setPayload(Map.of("id", 2, "nombre", "Luis"));

        svc.processProviderFromCore(m);

        ArgumentCaptor<ProviderData> cap = ArgumentCaptor.forClass(ProviderData.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getName()).startsWith("Luis");
    }

    @Test
    void procesa_baja_desactiva_aun_si_no_existe() {
        ProviderDataRepository repo = mock(ProviderDataRepository.class);
        when(repo.findByProviderId(3L)).thenReturn(Optional.empty());

        ProviderEventProcessorService svc = new ProviderEventProcessorService(repo);

        CoreEventMessage m = new CoreEventMessage();
        CoreEventMessage.Destination d = new CoreEventMessage.Destination();
        d.setChannel("catalogue.prestador.baja");
        m.setDestination(d);
        m.setPayload(Map.of("id", 3));

        svc.processProviderFromCore(m);

        ArgumentCaptor<ProviderData> cap = ArgumentCaptor.forClass(ProviderData.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getActive()).isFalse();
        assertThat(cap.getValue().getProviderId()).isEqualTo(3L);
    }

    @Test
    void ignora_evento_desconocido_y_no_guarda() {
        ProviderDataRepository repo = mock(ProviderDataRepository.class);
        ProviderEventProcessorService svc = new ProviderEventProcessorService(repo);

        CoreEventMessage m = new CoreEventMessage();
        CoreEventMessage.Destination d = new CoreEventMessage.Destination();
        d.setChannel("otra.cosa.evento");
        m.setDestination(d);
        m.setPayload(Map.of("id", 4));

        svc.processProviderFromCore(m);

        verify(repo, never()).save(any());
    }

    @Test
    void si_falta_id_no_hace_nada() {
        ProviderDataRepository repo = mock(ProviderDataRepository.class);
        ProviderEventProcessorService svc = new ProviderEventProcessorService(repo);

        CoreEventMessage m = new CoreEventMessage();
        CoreEventMessage.Destination d = new CoreEventMessage.Destination();
        d.setEventName("alta_prestador");
        m.setDestination(d);
        m.setPayload(Map.of()); // sin id

        svc.processProviderFromCore(m);

        verify(repo, never()).save(any());
    }
}
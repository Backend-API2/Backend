package backend_api.Backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

@SpringBootTest
public class TimeZoneTest {

    @Test
    public void testDefaultTimeZoneIsArgentina() {
        // Verificar que la zona horaria por defecto es Argentina
        TimeZone defaultTimeZone = TimeZone.getDefault();
        System.out.println("🕐 Zona horaria por defecto: " + defaultTimeZone.getID());

        assertEquals("America/Argentina/Buenos_Aires", defaultTimeZone.getID(),
                "La zona horaria por defecto debe ser America/Argentina/Buenos_Aires");
    }

    @Test
    public void testLocalDateTimeUsesArgentinaTime() {
        // Obtener la hora actual
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedNow = ZonedDateTime.now();

        System.out.println("🕐 LocalDateTime.now(): " + now);
        System.out.println("🕐 ZonedDateTime.now(): " + zonedNow);
        System.out.println("🕐 Zona: " + zonedNow.getZone());
        System.out.println("🕐 Offset: " + zonedNow.getOffset());

        // Verificar que la zona es Argentina
        ZoneId expectedZone = ZoneId.of("America/Argentina/Buenos_Aires");
        assertEquals(expectedZone, zonedNow.getZone(),
                "ZonedDateTime debe usar la zona horaria de Argentina");
    }

    @Test
    public void testTimeZoneOffsetIsMinusThree() {
        // Verificar que el offset es UTC-3
        ZonedDateTime zonedNow = ZonedDateTime.now();
        String offset = zonedNow.getOffset().toString();

        System.out.println("🕐 Offset actual: " + offset);

        // Argentina puede estar en -03:00 (horario estándar)
        assertEquals("-03:00", offset,
                "El offset debe ser -03:00 (UTC-3 para Argentina)");
    }
}

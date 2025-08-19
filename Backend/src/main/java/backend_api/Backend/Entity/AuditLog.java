package backend_api.Backend.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "audit_logs")
@Data
@Entity
public class AuditLog {
    
}

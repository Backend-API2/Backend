package backend_api.Backend.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserUpdatedMessage extends BaseMessage {
    private Long userId;
    private String email;
    private String role;
    private String dni;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private List<UserCreatedMessage.AddressInfo> address;
    private List<String> zones;
    private List<String> skills;
    private LocalDateTime updatedAt;
}

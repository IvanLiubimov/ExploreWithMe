package stat.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HitDtoRequest {
    private long id;
    private String app;
    private String uri;
    private String ip;
}

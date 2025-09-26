package stat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HitDtoRequest {
    private long id;
    private String app;
    private String uri;
    private String ip;
}

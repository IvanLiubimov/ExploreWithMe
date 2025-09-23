package stat.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HitDtoStatResponse {

    private String app;

    private String uri;

    private long hits;
}

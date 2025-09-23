package stat.server.model;

import stat.dto.HitDtoRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HitMapper {

    public Hit dtoRequestToModel (HitDtoRequest hitDtoRequest, Instant dateTime){
        return new Hit(hitDtoRequest.getId(),
                hitDtoRequest.getApp(),
                hitDtoRequest.getUri(),
                hitDtoRequest.getIp(), dateTime);
    }

}

package com.xyz.booking.client;

import com.xyz.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TheatreServiceClient {

    private final RestTemplate restTemplate;

    @Value("${theatre.service.url}")
    private String theatreServiceUrl;

    /**
     * Fetches seat list for a show from Theatre Service.
     * Returns a list of seat maps with seatNumber and status fields.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSeatsForShow(UUID showId) {
        String url = theatreServiceUrl + "/api/theatres/shows/" + showId + "/seats";
        try {
            var response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<ApiResponse<List<Map<String, Object>>>>() {});
            if (response.getBody() != null && response.getBody().isSuccess()) {
                return response.getBody().getData();
            }
        } catch (Exception e) {
            log.error("Failed to fetch seats for show {}: {}", showId, e.getMessage());
        }
        return List.of();
    }
}

package com.evroaminghub.station.config;

import com.evroaminghub.station.ocpp.OcppWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final OcppWebSocketHandler ocppWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(ocppWebSocketHandler, "/ocpp/{chargePointId}")
            .setAllowedOrigins("*");  // In production, restrict to known charge point IPs
    }
}

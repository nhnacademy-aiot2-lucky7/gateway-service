package com.nhnacademy.gateway.common.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.nhnacademy.gateway.common.enums.IoTProtocol;

import java.io.IOException;

public class IoTProtocolDeserializer extends JsonDeserializer<IoTProtocol> {

    @Override
    public IoTProtocol deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String raw = p.getText();
        return IoTProtocol.from(raw);
    }
}

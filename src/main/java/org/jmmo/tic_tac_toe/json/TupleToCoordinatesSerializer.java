package org.jmmo.tic_tac_toe.json;

import com.datastax.driver.core.TupleValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class TupleToCoordinatesSerializer extends JsonSerializer<TupleValue> {

    @Override
    public void serialize(TupleValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", value.getInt(0));
        gen.writeNumberField("y", value.getInt(1));
        gen.writeEndObject();
    }
}

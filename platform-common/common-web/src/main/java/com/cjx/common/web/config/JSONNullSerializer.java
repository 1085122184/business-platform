package com.cjx.common.web.config;

import cn.hutool.json.JSONNull;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class JSONNullSerializer extends StdSerializer<JSONNull> {
    public JSONNullSerializer() {
        super(JSONNull.class);
    }

    @Override
    public void serialize(
            JSONNull value,
            JsonGenerator gen,
            SerializerProvider provider) throws IOException {

        // 核心逻辑：直接写入标准的 JSON null 字面量
        gen.writeNull();
    }
}

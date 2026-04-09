package com.oagp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonService {

    private final ObjectMapper mapper;

    public JsonService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }
}

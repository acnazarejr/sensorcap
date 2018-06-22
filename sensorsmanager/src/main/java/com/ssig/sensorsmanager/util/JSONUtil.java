package com.ssig.sensorsmanager.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.ssig.sensorsmanager.SensorType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;

public final class JSONUtil {


    public static <T> void save(T object, String fileToSave) throws IOException, NullPointerException {
        JSONUtil.save(object, new File(fileToSave));
    }

    public static <T> void save(T object, File fileToSave) throws IOException, NullPointerException {
        if (object == null){
            throw new NullPointerException("The object is null.");
        }
        Writer writer = new FileWriter(fileToSave);
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .registerTypeAdapter(SensorType.class, new JSONUtil.SensorTypeSerializer());
        gsonBuilder.create().toJson(object, writer);
        writer.flush();
        writer.close();
    }

    public static <T> T load(String fileToLoad, Type typeOfT) throws FileNotFoundException {
        return JSONUtil.load(new File(fileToLoad), typeOfT);
    }

    public static <T> T load(File fileToLoad, Type typeOfT) throws FileNotFoundException {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .serializeNulls()
                .registerTypeAdapter(SensorType.class, new JSONUtil.SensorTypeDeserializer());
        return gsonBuilder.create().fromJson(new FileReader(fileToLoad), typeOfT);
    }


    private static class SensorTypeSerializer implements JsonSerializer<SensorType>{
        @Override
        public JsonElement serialize(SensorType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.code());
        }
    }

    private static class SensorTypeDeserializer implements JsonDeserializer<SensorType>{

        @Override
        public SensorType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return SensorType.fromCode(json.getAsString());
        }
    }

}

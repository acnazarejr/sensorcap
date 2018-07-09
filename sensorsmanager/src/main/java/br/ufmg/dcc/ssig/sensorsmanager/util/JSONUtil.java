package br.ufmg.dcc.ssig.sensorsmanager.util;

import android.hardware.Sensor;

import com.google.gson.*;
import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.SensorWriterType;

import java.io.*;
import java.lang.reflect.Type;

@SuppressWarnings("unused")
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
                .registerTypeAdapter(SensorType.class, new JSONUtil.SensorTypeSerializer())
                .registerTypeAdapter(SensorWriterType.class, new JSONUtil.SensorWriterTypeSerializer());
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
                .registerTypeAdapter(SensorType.class, new JSONUtil.SensorTypeDeserializer())
                .registerTypeAdapter(SensorWriterType.class, new JSONUtil.SensorWriterTypeDeserializer());
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

    private static class SensorWriterTypeSerializer implements JsonSerializer<SensorWriterType>{
        @Override
        public JsonElement serialize(SensorWriterType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.code());
        }
    }

    private static class SensorWriterTypeDeserializer implements JsonDeserializer<SensorWriterType>{
        @Override
        public SensorWriterType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return SensorWriterType.fromCode(json.getAsString());
        }
    }


}

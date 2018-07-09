package br.ufmg.dcc.ssig.sensorsmanager;

import java.util.Objects;

public enum SensorWriterType {

    BINARY("BIN"), CSV("CSV");

    private final String code;

    SensorWriterType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static SensorWriterType fromCode(String code) {
        for(SensorWriterType type : SensorWriterType.values()) {
            if(Objects.equals(code, type.code())) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        switch (this){
            case BINARY: return "BINARY";
            case CSV: return "CSV";
            default: return "BINARY";
        }
    }

    public String fileExtension() {
        switch (this){
            case BINARY: return "dat";
            case CSV: return "csv";
            default: return "dat";
        }
    }

}

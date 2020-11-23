package com.coldblock.coldet.icon;

import java.io.Serializable;

public class SerializedSignedTransaction implements Serializable {
    private static final long serialVersionUID = 1005;

    public String properties;

    public SerializedSignedTransaction(String properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "SerializedSignedTransaction{" +
                "properties:" + properties +
                "}";
    }
}

package com.coldblock.coldet.icon;

import org.bouncycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.icx.Transaction;
import foundation.icon.icx.transport.jsonrpc.RpcItem;

public class SerializedUnsignedTransaction implements Serializable {
    private static final long serialVersionUID = 1004;
    private final BigInteger version;
    private final String from;
    private final String to;
    private final BigInteger value;
    private final BigInteger stepLimit;
    private final BigInteger timeStamp;
    private final BigInteger nid;
    private final BigInteger nonce;
    private final String dataType;
    //private final RpcItem data;
    // private final RpcObject properties;

    public SerializedUnsignedTransaction(Transaction transaction){
        this.version = transaction.getVersion();
        this.from = transaction.getFrom().getPrefix().getValue() + Hex.toHexString(transaction.getFrom().getBody());
        this.to = transaction.getTo().getPrefix().getValue() + Hex.toHexString(transaction.getTo().getBody());
        this.value = transaction.getValue();
        this.stepLimit = transaction.getStepLimit();
        this.timeStamp = transaction.getTimestamp();
        this.nid = transaction.getNid();
        this.nonce = transaction.getNonce();
        this.dataType = transaction.getDataType();
        // this.data = transaction.getData();
        // this.properties = transaction.getProperties();
    }

    public String getFrom(){
        return this.from;
    }

    public String getTo(){
        return this.to;
    }

    public BigInteger getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return "SerializedUnsignedTransaction{" +
                "version=" + version +
                ", from=" + from +
                ", to=" + to +
                ", value=" + value +
                ", stepLimit=" + stepLimit +
                ", timeStamp=" + timeStamp +
                ", nid=" + nid +
                ", nonce=" + nonce +
                ", dataType='" + dataType + '\'' +
                '}';
    }

}

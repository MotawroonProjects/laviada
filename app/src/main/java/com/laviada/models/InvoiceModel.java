package com.laviada.models;

import java.io.Serializable;

public class InvoiceModel extends StatusResponse implements Serializable {

    private String data;

    public String getData() {
        return data;
    }
}

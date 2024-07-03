package org.dspace.app.rest.model;

import java.util.ArrayList;
import java.util.List;

public class PdfDTO {

    private List<String>bitstreamuuids=new ArrayList<>();

    public List<String> getBitstreamuuids() {
        return bitstreamuuids;
    }

    public void setBitstreamuuids(List<String> bitstreamuuids) {
        this.bitstreamuuids = bitstreamuuids;
    }
}

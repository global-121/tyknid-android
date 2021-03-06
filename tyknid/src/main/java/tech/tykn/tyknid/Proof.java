package tech.tykn.tyknid;

import java.util.ArrayList;
import java.util.List;

public class Proof {

    private String proofJson;

    public String getProofJson() {
        return proofJson;
    }

    public void setProofJson(String proofJson) {
        this.proofJson = proofJson;
    }

    public List<String> getSchemaIds() {
        return schemaIds;
    }

    public void setSchemaIds(List<String> schemaIds) {
        this.schemaIds = schemaIds;
    }

    public ArrayList<String> getCredDefinationIds() {
        return credDefinationIds;
    }

    public void setCredDefinationIds(ArrayList<String> credDefinationIds) {
        this.credDefinationIds = credDefinationIds;
    }

    private List<String> schemaIds;
    private ArrayList<String> credDefinationIds;

}
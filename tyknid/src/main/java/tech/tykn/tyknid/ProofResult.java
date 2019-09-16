package tech.tykn.tyknid;

public class ProofResult {

    public Proof getProof() {
        return proof;
    }

    public void setProof(Proof proof) {
        this.proof = proof;
    }

    public String getProofRequestJsonData() {
        return proofRequestJsonData;
    }

    public void setProofRequestJsonData(String proofRequestJsonData) {
        this.proofRequestJsonData = proofRequestJsonData;
    }



    private Proof proof;
    private String proofRequestJsonData;
}

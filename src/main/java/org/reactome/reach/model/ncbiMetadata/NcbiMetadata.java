package org.reactome.reach.model.ncbiMetadata;

public class NcbiMetadata {
    private Header header;
    private Result result;

    public void setHeader(Header header) {
        this.header = header;
    }
    public Header getHeader() {
        return header;
    }
    public void setResult(Result result) {
        this.result = result;
    }
    public Result getResult() {
        return result;
    }
}

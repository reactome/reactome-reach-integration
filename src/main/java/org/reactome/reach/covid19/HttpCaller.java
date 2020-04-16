package org.reactome.reach.covid19;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.gk.reach.ReachUtils;
import org.gk.reach.model.fries.FriesObject;

public class HttpCaller {

    public HttpCaller() {
    }

    /**
     * @param Path
     * @return FriesObject
     * @throws IOException
     */
    public FriesObject callReachAPI(Path paper) throws IOException {
        // TODO check that query file is valid.
        // TODO call REACH instance and return result.
        String url = "http://localhost:8080/api/uploadFile";
        Content content = Request.Post(url).bodyFile(paper.toFile(), ContentType.DEFAULT_TEXT).execute().returnContent();

        if (Thread.currentThread().isInterrupted())
            return null;
        FriesObject friesObject = ReachUtils.readJsonText(content.asString());
        return friesObject;
    }
    
    Content callHttpGet(URI url) throws IOException {
        return Request.Get(url).execute().returnContent();
    }
    
}

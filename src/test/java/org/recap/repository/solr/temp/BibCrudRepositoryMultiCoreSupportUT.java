package org.recap.repository.solr.temp;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Value;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by premkb on 1/8/16.
 */
public class BibCrudRepositoryMultiCoreSupportUT extends BaseTestCase{

    @Value("${solr.url}")
    String solrUrl;

    @Test
    public void instantiateBibCrudRepositoryMultiCoreSupport(){
        assertNotNull(new BibCrudRepositoryMultiCoreSupport("temp",solrUrl));
    }
}

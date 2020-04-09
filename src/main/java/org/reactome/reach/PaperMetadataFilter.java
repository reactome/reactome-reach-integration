package org.reactome.reach;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.exceptions.CsvConstraintViolationException;

public class PaperMetadataFilter implements BeanVerifier<PaperMetadata> {

    public PaperMetadataFilter() {
    }

    @Override
    public boolean verifyBean(PaperMetadata bean) throws CsvConstraintViolationException {
        boolean noncomm = bean.getFull_text_file().equals("noncomm_use_subset");
        boolean hasPmcXmlParse = bean.getHasPmc_xml_parse().equals("True");

        if (noncomm && !hasPmcXmlParse)
            return true;

        return false;
    }

}

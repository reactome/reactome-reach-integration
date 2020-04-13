package org.reactome.reach;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.exceptions.CsvConstraintViolationException;

public class PaperMetadataFilter implements BeanVerifier<scholarMetadata> {

    public PaperMetadataFilter() {
    }

    @Override
    public boolean verifyBean(scholarMetadata bean) throws CsvConstraintViolationException {
        if (bean.getFull_text_file() == null)
            return false;

        if (bean.getFull_text_file().equals("noncomm_use_subset"))
            return true;

        return false;
    }

}

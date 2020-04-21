package org.reactome.reach.covid19;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.exceptions.CsvConstraintViolationException;

public class ScholarMetadataFilter implements BeanVerifier<ScholarMetadata> {

    public ScholarMetadataFilter() {
    }

    /**
     * Filter out rows that do not have full text files and are not in the
     * 'noncommercial' dataset.
     */
    @Override
    public boolean verifyBean(ScholarMetadata bean) throws CsvConstraintViolationException {
        if (bean.getFull_text_file() == null)
            return false;

        if (bean.getFull_text_file().equals("noncomm_use_subset"))
            return true;

        return false;
    }

}

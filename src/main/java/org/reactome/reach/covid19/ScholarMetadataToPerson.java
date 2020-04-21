package org.reactome.reach.covid19;

import java.util.Arrays;
import java.util.List;

import org.gk.model.Person;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class ScholarMetadataToPerson extends AbstractCsvConverter {

    /**
     * Convert name string to Person (e.g. 'Alyssa P. Hacker' -> Person object).
     */
    @Override
    public Object convertToRead(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        Person person = new Person();

        // last name
        List<String> split = Arrays.asList(value.split(", "));
        person.setLastName(split.get(0));

        // first name
        if (split.size() == 2) {
            split = Arrays.asList(split.get(1).split(" "));
            person.setFirstName(split.get(0));
        }

        // initial
        if (split.size() == 2)
            person.setInitial(split.get(1).replaceAll("\\.", ""));

        return person;
    }

}

# Class Overview

    The following directories will be created and populated with the
    described output --

      Step   Class/Operation            Path                Description
      ------ -------------------------- ------------------- ---------------------------------------------------------------
      1      SemanticScholarConverter   semantic-scholar/   Dataset of JSON files and metadata file from Semantic Scholar

      2      REACH                      reach/papers        NXML, CSV/TSV, or plain text input for REACH.
      3                                 reach/output        FRIES output (events, entities, sentences triplet).

      4      FriesMerger                fries/merged        Triplet files merged into one FRIES file.
      5      FriesFilter                fries/filtered      FRIES files with at least one such event.
      6      FriesReference             fries/references    Fries Files with \"Reference\" field appended.

# Directory Overview

-   `$HOME/Documents/reach-to-frext` is the \"rootDir\" directory as
specified in REACH\'s \"application.conf\" file. `restart.log` is a list
of filenames that have already been processed for a given operation, and
may safely be skipped.

-   Root directory (timestamped directory created every Sem. Scholar
    download (weekly Cron job) -- only need to process new files).

    ``` {.example}
    $HOME/Documents/reach-to-frext/current -> 2020-04-01T12:00:00
    │
    ├── semantic-scholar (1)
    │   │
    │   ├── c1ad13d83e926979dbf2bbe52e4944082f28dfea.json
    │   └── metadata.csv
    │
    ├── reach
    │   │
    │   ├── papers (2)
    │   │   └── PMC1616946.txt
    │   │   └── restart.log (only process new files from dataset).
    │   │
    │   ├── output (3)
    │   │   ├── PMC1616946.entities.json
    │   │   ├── PMC1616946.events.json
    │   │   ├── PMC1616946.sentences.json
    │   │   └── restart.log
    │   │
    │   └── reach.log
    │
    └── fries
        │
        ├── merged (4)
        │   ├── PMC1616946.fries.json
        │   └── restart.log
        │
        ├── filtered (5)
        │   ├── PMC1616946.fries.json
        │   └── restart.log
        │
        └── references (6)
            ├── PMC1616946.fries.json
            └── restart.log
    ```


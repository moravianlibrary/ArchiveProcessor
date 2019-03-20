## ArchiveProcessor [![Build Status](https://travis-ci.org/moravianlibrary/ArchiveProcessor.svg?branch=master)](https://travis-ci.org/moravianlibrary/ArchiveProcessor)

Tool for processing ML archive input data in BagIt format.

### Building

For building jar archive use `gradlew build`

### Running

#### Processing

Processing is intended to be run on daily basis to process new data to be archived. To start processing - run with first argument `process`.

Run without any additional arguments to display help. To start processing use:
- `-i path` for input directory path
- `-o path` for output/temporary archive directory path
- `-e path` for error directory path

Application processes each directory in input directory. Directory names must be one of Aleph-accepted identifiers (barcode, sysno, signature), from this identifier the archive path is determined (base + sysno). If any error occurs during the archivation process the erroreous input is moved into error directory.

#### Archiving

Archiving is intended to be run on yearly basis to create large zip archive of data that were processed that year. To start archivation process - run with first argument `archive`.

Run without any additional arguments to display help. To start processing use:
- `-i path` for input directory path (input is a directory containing data from year to be archived)
- `-o path` for output/permanent archive directory path 

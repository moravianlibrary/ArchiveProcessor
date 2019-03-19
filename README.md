## ArchiveProcessor [![Build Status](https://travis-ci.org/moravianlibrary/ArchiveProcessor.svg?branch=master)](https://travis-ci.org/moravianlibrary/ArchiveProcessor)

Tool for processing ML archive input data in BagIt format.

### Building

For building jar archive use `gradlew build`

### Running

Run without arguments to display help. To start processing use:
- `-i path` for input directory path
- `-o path` for output/archive directory path
- `-e path` for error directory path

Application processes each directory in input directory. Directory names must be one of Aleph-accepted identifiers (barcode, sysno, signature), from this identifier the archive path is determined (base + sysno). If any error occurs during the archivation process the erroreous input is moved into error directory.

# TSV VCF Converter

##Usage:
```
usage: java -jar vip-tsv-vcf-converter.jar -i <arg> -m <arg> -o <arg> [-f]
       [-d]
 -i,--input <arg>      .tsv input file containing at least a chromosome, position, reference and alternative column.
 -m,--mappings <arg>   Mapping for position columns.
 -o,--output <arg>     Output file.
 -f,--force            Override the output files if it already exists.
 -d,--debug            Enable debug mode (additional logging).

usage: java -jar vip-tsv-vcf-converter.jar -v
 -v,--version   Print version.

Process finished with exit code 64
```

##Example:
```-i /path/to/test.tsv -m "CHROM=chromosome,POS=start,STOP=stop,REF=ref,ALT=alt" -o /path/to/test.vcf.gz -f```

##Mapping:
Comma separated list of key value pairs where the key is one of the fields below, and the value de header value in your file.

Required columns: CHROM, POS, REF, ALT
Optional column: STOP
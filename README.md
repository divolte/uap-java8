# User agent string parser based on BrowserScope data
This is a Java8 user agent parser implementation based on the data collected by the [uap-core project](https://github.com/ua-parser/uap-core). In oder to use this parser, you need a version of the the parser database from here: https://github.com/ua-parser/uap-core/blob/master/regexes.yaml. This file is not distributed as part of this repository.

## Usage

```java
Parser p = new Parser(
        // Obtain this file from: https://github.com/ua-parser/uap-core/blob/master/regexes.yaml
        // It's not distributed with this project.
        Parser.class.getResourceAsStream("/regexes.yaml"),
        // Leniency flag; the regexes may contain invalid configs
        // By passing in true here; the parser will ignore these
        true);

String input = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) "
             + "AppleWebKit/537.36 (KHTML, like Gecko) "
             + "Chrome/40.0.2214.93 Safari/537.36";

// These classes expose getters for user agent fields.
UserAgent agent = p.parseUserAgent(input);
Device device = p.parseDevice(input);
OperatingSystem os = p.parseOperatingSystem(input);
```

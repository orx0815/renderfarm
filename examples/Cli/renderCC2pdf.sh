 #!/bin/bash
 echo 'rendering pdfs from *_pdfCfg.json'
 java -Djava.awt.headless=true -jar ./target/io.wcm.renderfarm.cli-0.1.0-SNAPSHOT.jar
# mvn exec:java -Dexec.mainClass="io.wcm.renderfarm.misc.cli.Cli"

 #!/bin/bash
 echo 'rendering pdfs from *_pdfCfg.json'
 java -Djava.awt.headless=true -jar ./target/org.motorbrot.renderfarm.cli-0.1.0-SNAPSHOT.jar
 # mvn exec:java -Dexec.mainClass="org.motorbrot.renderfarm.misc.cli.Cli"

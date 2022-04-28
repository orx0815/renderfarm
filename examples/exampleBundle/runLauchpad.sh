 # run sling standalone
java -Dorg.osgi.framework.bootdelegation=javax.imageio,javax.imageio.*,org.w3c.dom,org.w3c.dom.*,javax.xml.parsers,javax.xml.transform.dom,com -jar org.apache.sling.launchpad-8.jar

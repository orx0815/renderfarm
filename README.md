
# InkForBatik
##### Using [Inkscape] as WYSIWYG editor for dynamic, server-side Pdf rendering in Java

Directory contents:

-  **renderfarm-service**: (core "library")
Bridge from [Inkscape] to [Batik], including svg1.2 flow-text.
 [Solved in 2007], the InkscapeToBatikPreprocessor.java transforms a svg-dom produced by Inkscape to a form that batik likes.
 The RenderFarm Service around this takes a data-model that defines a documet with pages and their dynamic texts/images. It parses the referened SVGs, modifies their DOM with dynamic content, generates single-page PDFs and merges them at the end with [Apache PDFBox®].
 
 -  **examples**
 Two usages of RenderFarm, one command-line tool (parses json files) and one [Apache Sling]-Servlet.
 
 - **showcase**
 SVGs and bitmaps, used in both examples
 
 - **batik-osgi-bundle**
OSGi-wrapper for FOP and Batik. Bundle only needs to be installed, when deploying renderfarm into in sling or AEM.
 
 
 [Solved in 2007]: <https://mail-archives.apache.org/mod_mbox/xmlgraphics-batik-users/200704.mbox/%3C461C1DEB.7040401@gmx.de%3E>
 [Apache PDFBox®]: <https://pdfbox.apache.org/>
 [Apache Sling]:<https://sling.apache.org/>

## Status
Using Apache FOP in version 2.2
In version 2.4 the Avalon configuration dependency was dropped from FOP. But here this is used to embedd custom fonts within the PDF.  
So an upgrade to 2.7+ is a toDo.

https://xmlgraphics.apache.org/fop/2.4/releaseNotes_2.4.html


## Background
 
### Inkscape
Inkscape is a professional vector graphics editor for Windows, Mac OS X and Linux. It's free and open source.
Inscape's native file format is SVG, which is  an open XML-based W3C standard. 

The idea is to create nice looking templates in Inkscape, put those xml-files into your java-application and generate dynamic Pdfs from them, by replacing text-nodes and bitmap-images on the fly.

### Apache™ FOP and XSL-FO
FOP uses the standard XSL-FO file format as input, lays the content out into pages, then renders it to the requested output, mostly Pdf.
It's an apache project since 1999, latest 2.2 release is from April 2017.

XSL-FO or "XSL Formatting Objects" (a W3C standard) was the most often used technology to generate PDF documents, from XML or XHTML content.
There have been a number of reports that [XSL-FO is dead] and that CSS3 Paged Media is ready to take over it's role as the industry standard for printed output.

A problem is that you'll have to develop an XSLT stylesheets that converts an XML to XSL-FO. That is quite a learning-curve to even get startet.

Instead we use FOP's ability to embed SVG. We have a full-page Svg from Inkscape and feed that to Batik. Batik then uses  FOP/XSL-FO only internally, without the need to even look at XML/XSLT.

### Batik

Batik is a pure-Java library that can be used to render, generate, and manipulate SVG graphics.
It was started in 2000, it's now together with FOP part of apache xml-graphics. In fact it's a dependency of FOP.
You can use Batik’s modules to convert SVG to various formats, such as raster images (JPEG, PNG or TIFF) or other vector formats (EPS or PDF), the latter two due to the transcoders provided by Apache FOP.

Thanks to an open w3c specification for SVG 1.1, Inkscape and Batik play together very well.
Batik was long the most conformant existing SVG 1.1 implementation.

From Inkscape's FAQ:
Q: Inkscape and renderer X show my SVGs differently. What to do?
A: That depends on X. We accept Batik and Adobe SVG plugin as authoritative SVG renderers because they are backed by some of the the authors of the SVG standard and really care about compliance.

## Solved Obstacles
#### SVG 1.2 Flowed Text debacle

When dealing with dynamic text, it's crucial that a long text flows automatically on multiple lines.
That is not possible with SVG 1.1, you need a separate <text> element for each line.

When you use the text-tool in Inkscape and click on the canvas you create a SVG 1.1 text. When you click and drag, you'll get a SVG 1.2 flowRoot that does exacly what we want.
Both Inkscape and Batik support the flowRoot Element.
Unfortunately both implementations base on a W3C specification that is a 'Working Draft' and both sides have used versions that are different regarding flowRoots.
Both sides said: "We fix it when spec's final.", quite understandably.
Twelve years later and SVG1.2 is still in working draft and will never take of.

SVG2 is in in "W3C Candidate Recommendation 15 September 2016" but does things differently with more CSS-3. Hard to tell if Batik will ever support SVG2.

The InkscapeToBatikPreprocessor.java in renderfarm-service project is a simple class that transforms a svg-dom produced by Inkscape to a form that batik likes.

#### Multithreading FOP
From https://xmlgraphics.apache.org/fop/2.2/embedding.html#multithreading :
Apache FOP may currently not be completely thread safe. [...] 

The Fop PDFTranscoder itself isn't threadsafe, so you have to instantiate on for each thread.
There is an issue with java.awt.color.ICC_Profile when running parallel. 
See RenderFram.java how it is workarounded. (By instantiating a static java.awt.color.ICC_Profile)

#### OSGi
The batik-bundle subproject provides an osgi-wrapper for FOP and Batik. Tested in sling and AEM

https://wiki.apache.org/xmlgraphics/OSGiSupport



[Inkscape]: <https://inkscape.org/en/>
[Batik]: <https://xmlgraphics.apache.org/batik/>
[XSL-FO is dead]: <http://www.rockweb.co.uk/blog/2014/06/xsl-fo-is-dead,-css-paged-media-is-prime-suspect/>
[batik in inkscape]: <https://inkscape.org/de/~satrio_jati/%E2%98%85batik-design>
[flowText in Inkscape]: <http://wiki.inkscape.org/wiki/index.php/Frequently_asked_questions#What_about_flowed_text.3F>
[flowText in Batik]: <https://xmlgraphics.apache.org/batik/dev/svg12.html#flowtext>
[SVG 1.2 Spec]: <https://www.w3.org/TR/SVG12/>
[Dmitry Baranovskiy - You Don't Know SVG]: <https://www.youtube.com/watch?v=SeLOt_BRAqc&t=15m40s>





 


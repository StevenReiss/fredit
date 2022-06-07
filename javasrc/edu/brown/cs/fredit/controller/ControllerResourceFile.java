/********************************************************************************/
/*                                                                              */
/*              ControllerResourceFile.java                                     */
/*                                                                              */
/*      description of class                                                    */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2013 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2013, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 *  Permission to use, copy, modify, and distribute this software and its        *
 *  documentation for any purpose other than its incorporation into a            *
 *  commercial product is hereby granted without fee, provided that the          *
 *  above copyright notice appear in all copies and that both that               *
 *  copyright notice and this permission notice appear in supporting             *
 *  documentation, and that the name of Brown University not be used in          *
 *  advertising or publicity pertaining to distribution of the software          *
 *  without specific, written prior permission.                                  *
 *                                                                               *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS                *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND            *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY      *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY          *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,              *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS               *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE          *
 *  OF THIS SOFTWARE.                                                            *
 *                                                                               *
 ********************************************************************************/



package edu.brown.cs.fredit.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

public class ControllerResourceFile implements ControllerConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          file_path;
private Element         file_contents;
private int             file_priority;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

ControllerResourceFile(Element xml) 
{
   file_path = IvyXml.getAttrString(xml,"NAME");
   file_priority = IvyXml.getAttrInt(xml,"PRIORITY");
   file_contents = IvyXml.loadXmlFromFile(file_path);
   if (file_contents == null && file_path != null) {
      int idx = file_path.indexOf("!/");
      if (idx > 0) {
         String jarnm = file_path.substring(0,idx);
         if (jarnm.startsWith("file:")) jarnm = jarnm.substring(5);
         String eltnm = file_path.substring(idx+2);
         try {
            ZipFile jf = new ZipFile(jarnm);
            ZipEntry ent = jf.getEntry(eltnm);
            if (ent != null) {
               InputStream ins = jf.getInputStream(ent);
               file_contents = IvyXml.loadXmlFromStream(ins);
             }
            jf.close();
          }
         catch (IOException e) {
            System.err.println("Problem with zip file: " + e);
            e.printStackTrace();
          }
       }
    }
   
   if (file_contents == null) {
      System.err.println("FREDIT: Can't load resource file " + file_path);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public int getPriority()                { return file_priority; }
public String getFile()                 { return file_path; }
public Element getContents()            { return file_contents; }



}       // end of class ControllerResourceFile




/* end of ControllerResourceFile.java */


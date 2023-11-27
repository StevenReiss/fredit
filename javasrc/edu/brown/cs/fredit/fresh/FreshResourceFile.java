/********************************************************************************/
/*                                                                              */
/*              FreshResourceFile.java                                          */
/*                                                                              */
/*      Hold information about a file                                           */
/*                                                                              */
/********************************************************************************/
/*      Copyright 2011 Brown University -- Steven P. Reiss                    */
/*********************************************************************************
 *  Copyright 2011, Brown University, Providence, RI.                            *
 *                                                                               *
 *                        All Rights Reserved                                    *
 *                                                                               *
 * This program and the accompanying materials are made available under the      *
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, *
 * and is available at                                                           *
 *      http://www.eclipse.org/legal/epl-v10.html                                *
 *                                                                               *
 ********************************************************************************/

/* SVN: $Id$ */



package edu.brown.cs.fredit.fresh;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.file.IvyLog;
import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

class FreshResourceFile implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          file_path;
private int             file_priority;
private int             file_count;
private List<FreshSubtypeImpl> file_subtypes;
private List<FreshSafetyConditionImpl> safety_conditions;
private List<FreshMethodData> method_data;
private List<String>    load_classes;
private Map<String,AllocMode> alloc_data;
private boolean         is_editable;

private static AtomicInteger all_counter = new AtomicInteger(0);



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshResourceFile(Element xml) 
{
   file_path = IvyXml.getAttrString(xml,"NAME");
   file_priority = IvyXml.getAttrInt(xml,"PRIORITY");
   Element contents = IvyXml.loadXmlFromFile(file_path);
   file_count = all_counter.incrementAndGet();
   is_editable = IvyXml.getAttrBool(xml,"BASE_PROJECT");
   
   if (contents == null && file_path != null) {
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
               contents = IvyXml.loadXmlFromStream(ins);
             }
            jf.close();
          }
         catch (IOException e) {
            IvyLog.logE("FRESH","Problem with zip file",e);
          }
       }
    }
   
   if (contents == null) {
      System.err.println("FREDIT: Can't load resource file " + file_path);
      IvyLog.logE("FRESH","Can't load resource file " + file_path);
    }
   
   file_subtypes = new ArrayList<>();
   safety_conditions = new ArrayList<>();
   method_data = new ArrayList<>();
   load_classes = new ArrayList<>();
   alloc_data = new LinkedHashMap<>();
   for (Element sub : IvyXml.children(contents)) {
      switch (sub.getNodeName()) {
         case "SUBTYPE" :
            file_subtypes.add(new FreshSubtypeImpl(sub));
            break;
         case "SAFETY" :
            safety_conditions.add(new FreshSafetyConditionImpl(sub));
            break;
         case "PACKAGE" :
         case "CLASS" :
         case "METHOD" :
            method_data.add(new FreshMethodData(sub));
            break;
         case "ALLOC" :
            String allocnm = IvyXml.getAttrString(xml,"CLASS");
            boolean ast = IvyXml.getAttrBool(xml,"AST");
            boolean inherit = IvyXml.getAttrBool(xml,"INHERIT");
            boolean local = IvyXml.getAttrBool(xml,"LOCAL");
            AllocMode am = AllocMode.BINARY;
            if (local) am = AllocMode.LOCAL;
            else if (ast) {
               if (inherit) am = AllocMode.AST_INHERIT;
               else am = AllocMode.AST;
             }
            else if (inherit) am = AllocMode.BINARY_INHERIT;
            if (allocnm != null) alloc_data.put(allocnm,am);
            break;
         case "LOAD" :
            String loadnm = IvyXml.getAttrString(xml,"CLASS");
            if (loadnm == null) loadnm = IvyXml.getAttrString(xml,"NAME");
            if (loadnm != null) load_classes.add(loadnm);
            break;
         default :
            IvyLog.logE("FAIT","Unknown resource element " + sub);
            break;
            
       }
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

int getPriority()                               { return file_priority; }
int getCount()                                  { return file_count; }
String getFile()                                { return file_path; }
boolean isEditable()                            { return is_editable; }
List<FreshMethodData> getMethodData()           { return method_data; }
List<FreshSubtypeImpl> getSubtypes()            { return file_subtypes; }

List<FreshSafetyConditionImpl> getSafetyConditions()
{
   return safety_conditions;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw)
{
   xw.begin("FAIT");
   for (FreshSubtypeImpl subtype : file_subtypes) {
      subtype.outputXml(xw);
    }
   for (FreshSafetyConditionImpl safety : safety_conditions) {
      safety.outputXml(xw);
    }
   for (FreshMethodData method : method_data) {
      method.outputXml(xw);
    }
   for (String s : load_classes) {
      xw.begin("LOAD");
      xw.field("CLASS",s);
      xw.end("LOAD");
    }
   for (Map.Entry<String,AllocMode> ent : alloc_data.entrySet()) {
      xw.begin("ALLOC");
      xw.field("CLASS",ent.getKey());
      switch (ent.getValue()) {
         case LOCAL :
            xw.field("LOCAL",true);
            break;
         case AST :
            xw.field("AST",true);
            break;
         case AST_INHERIT :
            xw.field("AST",true);
            xw.field("INHERIT",true);
            break;
         case BINARY :
            break;
         case BINARY_INHERIT :
            xw.field("INHERIT",true);
            break;
            
       }
    }
}


}       // end of class FreshResourceFile




/* end of FreshResourceFile.java */


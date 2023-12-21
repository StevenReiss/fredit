/********************************************************************************/
/*                                                                              */
/*              FreshManger.java                                                */
/*                                                                              */
/*      Manager of Fait RESourse Helper data                                    */
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

public class FreshManager implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private List<FreshResourceFile> resource_files;
private FreshResourceFile editable_file;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

public FreshManager()
{
   resource_files = new ArrayList<>();
}



/********************************************************************************/
/*                                                                              */
/*      Handle setting up resources                                             */
/*                                                                              */
/********************************************************************************/

public void setupResourceFiles(Element xml)
{
   resource_files.clear();
   
   Element files = xml;
   if (!IvyXml.isElement(xml,"FILES")) {
      files = IvyXml.getChild(xml,"FILES");
    }
   for (Element filexml : IvyXml.children(files,"FILE")) {
      FreshResourceFile crf = new FreshResourceFile(filexml);
      resource_files.add(crf);
    }
   
   // Files are kept in order so most important is last
   resource_files.sort(new PriorityComparator());
   editable_file = resource_files.get(resource_files.size()-1);
}


private static class PriorityComparator implements Comparator<FreshResourceFile> {
   
   @Override public int compare(FreshResourceFile f1,FreshResourceFile f2) {
      int v = f2.getPriority() - f1.getPriority();
      if (v != 0) return v;
      v = f1.getCount() - f2.getCount();
      return v;
    }
   
}       // end of inner class PriorityComparator




/********************************************************************************/
/*                                                                              */
/*      Subtype editing and access methods                                      */
/*                                                                              */
/********************************************************************************/

public FreshSubtype createSubtype(String name)
{
   return new FreshSubtypeImpl(name);
}


public FreshSubtypeValue createSubtypeValue(String name)
{
   return new FreshSubtypeValueImpl(name);
}



/********************************************************************************/
/*                                                                              */
/*      Safety condition editing and access methods                             */
/*                                                                              */
/********************************************************************************/

public FreshSafetyCondition createSafetyCondition(String name)
{
   return new FreshSafetyConditionImpl(name);
}



/********************************************************************************/
/*                                                                              */
/*      Method item editing                                                     */
/*                                                                              */
/********************************************************************************/

public void setSkipped(String name,MethodDataKind kind,boolean fg)
{
   FreshMethodData usedata = null;
   FreshResourceFile usefile = null;
   
   for (FreshResourceFile frf : resource_files) {
      for (FreshMethodData md : frf.getMethodData()) {
         if (md.getName().equals(name)) {
            usefile = frf;
            usedata = md;
          }
         else if (md.getName().startsWith(name)) {
//          outerdata = md;
          }
       }
    }
   
   if (usedata != null && usefile == editable_file) {
      if (usedata.isSkipped() != fg) {
         usedata.setSkipped(fg);
       }
    }
   else if (usedata != null) {
      usedata = usedata.clone();
      usedata.setSkipped(fg);
      editable_file.addMethodData(usedata);
    }
   else {
      usedata = new FreshMethodData(name,kind);
      usedata.setSkipped(fg);
      editable_file.addMethodData(usedata);
    }
}



/********************************************************************************/
/*                                                                              */
/*      Get list of items that are skipped during flow analysis                 */
/*                                                                              */
/********************************************************************************/

public Collection<FreshSkipItem> getSkippedItems()
{
   Map<String,FreshSkipItem> rslt = new HashMap<>();
   
   for (FreshResourceFile frf : resource_files) {
      for (FreshMethodData md : frf.getMethodData()) {
         if (md.isSkipped()) {
            rslt.put(md.getName(),md);
          }
         else {
            rslt.remove(md.getName());
          }
       }
    }
   
   return rslt.values();
}


/********************************************************************************/
/*                                                                              */
/*      Get the set of active subtypes                                          */
/*                                                                              */
/********************************************************************************/

public Collection<FreshSubtype> getSubtypes()
{
   Map<String,FreshSubtype> rslt = new HashMap<>();
   for (FreshResourceFile frf : resource_files) {
      for (FreshSubtypeImpl subtype : frf.getSubtypes()) {
         String nm = subtype.getName();
         if (nm == null) continue;
         FreshSubtypeImpl prior = (FreshSubtypeImpl) rslt.get(nm);
         if (prior != null) {
            // update prior with new information
          }
         else {
            rslt.put(nm,subtype);
          }
       }
    }
   return rslt.values();
}


/********************************************************************************/
/*                                                                              */
/*      Get the set of active safety conditions                                 */
/*                                                                              */
/********************************************************************************/

public Collection<FreshSafetyCondition> getSafetyConditions()
{
   Map<String,FreshSafetyCondition> rslt = new HashMap<>();
   for (FreshResourceFile frf : resource_files) {
      for (FreshSafetyConditionImpl subtype : frf.getSafetyConditions()) {
         String nm = subtype.getName();
         if (nm == null) continue;
         FreshSafetyConditionImpl prior = (FreshSafetyConditionImpl) rslt.get(nm);
         if (prior != null) {
            // update prior with new information
          }
         else {
            rslt.put(nm,subtype);
          }
       }
    }
   return rslt.values();
}


}       // end of class FreshManger




/* end of FreshManger.java */


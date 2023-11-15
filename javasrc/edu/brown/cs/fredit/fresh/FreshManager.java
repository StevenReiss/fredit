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
import java.util.Comparator;
import java.util.List;

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

public void addResourceFiles(Element xml)
{
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
}



private static class PriorityComparator implements Comparator<FreshResourceFile> {
   
   @Override public int compare(FreshResourceFile f1,FreshResourceFile f2) {
      int v = f2.getPriority() - f1.getPriority();
      if (v != 0) return v;
      v = f1.getCount() - f2.getCount();
      return v;
    }
   
}

}       // end of class FreshManger




/* end of FreshManger.java */


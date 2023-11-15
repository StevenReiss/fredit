/********************************************************************************/
/*                                                                              */
/*              FreshMethodWhen.java                                            */
/*                                                                              */
/*      Descriptionn of a WHEN condition for a MethodData item                  */
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshMethodWhen implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String caller_name;
private String caller_description;
private int instance_number;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshMethodWhen(Element xml) {
   caller_name = IvyXml.getAttrString(xml,"CALLER");
   caller_description = IvyXml.getAttrString(xml,"DESCRIPTION");
   instance_number = IvyXml.getAttrInt(xml,"INSTANCE",-1);
}


/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw) 
{
   xw.begin("WHEN");
   if (caller_name != null) xw.field("CALLER",caller_name);
   if (caller_description != null) xw.field("DESCRIPTION",caller_description);
   if (instance_number > 0) xw.field("INSTANCE",instance_number);
   xw.end("WHEN");
}



}       // end of class FreshMethodWhen




/* end of FreshMethodWhen.java */


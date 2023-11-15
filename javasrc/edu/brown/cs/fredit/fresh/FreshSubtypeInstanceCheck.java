/********************************************************************************/
/*                                                                              */
/*              FreshSubtypeInstanceCheck.java                                  */
/*                                                                              */
/*      Check for subtype value for instance uses (e.g. constants)              */
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

public class FreshSubtypeInstanceCheck implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private boolean is_primitive;
private boolean is_null;
private String for_value;
private String type_class;
private String value_class;
private FreshSubtypeValue result_value;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSubtypeInstanceCheck(FreshSubtype sd,Element xml) 
{
   is_primitive = IvyXml.getAttrBool(xml,"PRIMITIVE");
   is_null = IvyXml.getAttrBool(xml,"NULL");
   for_value = IvyXml.getAttrString(xml,"EQUALS");
   type_class = IvyXml.getAttrString(xml,"CLASS");
   value_class = IvyXml.getAttrString(xml,"CONSTCLASS");
   result_value = sd.getValue(IvyXml.getAttrString(xml,"RESULT"));
} 




/********************************************************************************/
/*                                                                              */
/*      Output Methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(String what,IvyXmlWriter xw) 
{
   xw.begin(what);
   
   if (is_primitive) xw.field("PRIMITIVE",is_primitive);
   if (is_null) xw.field("NULL",is_null);
   if (for_value != null) xw.field("EQUALS",for_value);
   if (type_class != null) xw.field("CLASS",type_class);
   if (value_class != null) xw.field("CONSTCLASS",value_class);
   if (result_value !=  null) xw.field("RESULT",result_value.getName());
   
   xw.end(what);
}


}       // end of class FreshSubtypeInstanceCheck




/* end of FreshSubtypeInstanceCheck.java */


/********************************************************************************/
/*                                                                              */
/*              FreshSubtypeValue.java                                          */
/*                                                                              */
/*      Possible value for a subtype                                            */
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSubtypeValue implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String value_name;
private Set<String> value_attributes;
private boolean is_default;
private boolean const_default;
private boolean uninit_default;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSubtypeValue(Element xml)
{
   value_name = IvyXml.getAttrString(xml,"NAME");
   
   value_attributes = new HashSet<>();
   String attrs = IvyXml.getAttrString(xml,"ATTRIBUTES");
   if (attrs != null) {
      StringTokenizer tok = new StringTokenizer(attrs);
      while (tok.hasMoreTokens()) {
         value_attributes.add(tok.nextToken());
       }
    }
   
   is_default = IvyXml.getAttrBool(xml,"DEFAULT");
   const_default = IvyXml.getAttrBool(xml,"CONSTANT");
   uninit_default = IvyXml.getAttrBool(xml,"UNINIT");
}




FreshSubtypeValue(String name) 
{
   value_name = name;
   value_attributes = new HashSet<>();
   is_default = false;
   const_default = false;
   uninit_default = false;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getName()                        { return value_name; }

Collection<String> getAttributes()      { return value_attributes; }

boolean isDefault()                     { return is_default; }

boolean isConstantDefault()             { return const_default; }

boolean isUninitDefault()               { return uninit_default; }



/********************************************************************************/
/*                                                                              */
/*      Output Methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw) 
{
   xw.begin("VALUE");
   xw.field("NAME",value_name);
   StringBuffer buf = new StringBuffer();
   for (String s : value_attributes) {
      if (!buf.isEmpty()) buf.append(" ");
      buf.append(s);
    }
   if (is_default) xw.field("DEFAULT",is_default);
   if (const_default) xw.field("CONSTANT",const_default);
   if (uninit_default) xw.field("UNINIT",uninit_default);
   xw.end("VALUE");
}


}       // end of class FreshSubtypeValue




/* end of FreshSubtypeValue.java */


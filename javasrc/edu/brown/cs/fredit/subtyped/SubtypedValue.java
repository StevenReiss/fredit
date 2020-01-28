/********************************************************************************/
/*                                                                              */
/*              SubtypedValue.java                                              */
/*                                                                              */
/*      Possible value of a subtype                                             */
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



package edu.brown.cs.fredit.subtyped;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

class SubtypedValue implements SubtypedConstants
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

SubtypedValue(Element xml)
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



SubtypedValue(String name) 
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



}       // end of class SubtypedValue




/* end of SubtypedValue.java */


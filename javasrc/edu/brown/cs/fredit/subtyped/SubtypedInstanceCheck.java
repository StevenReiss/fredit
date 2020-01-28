/********************************************************************************/
/*                                                                              */
/*              SubtypedInstanceCheck.java                                      */
/*                                                                              */
/*      Check for instance uses (e.g. constants)                                */
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

@SuppressWarnings("unused")
class SubtypedInstanceCheck implements SubtypedConstants
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
private SubtypedValue result_value;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedInstanceCheck(SubtypedDef sd,Element xml) 
{
   is_primitive = IvyXml.getAttrBool(xml,"PRIMITIVE");
   is_null = IvyXml.getAttrBool(xml,"NULL");
   for_value = IvyXml.getAttrString(xml,"EQUALS");
   type_class = IvyXml.getAttrString(xml,"CLASS");
   value_class = IvyXml.getAttrString(xml,"CONSTCLASS");
   result_value = sd.getValue(IvyXml.getAttrString(xml,"RESULT"));
} 




}       // end of class SubtypedInstanceCheck




/* end of SubtypedInstanceCheck.java */


/********************************************************************************/
/*                                                                              */
/*              SubtypedDef.java                                                */
/*                                                                              */
/*      Definition of a subtype                                                 */
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

@SuppressWarnings("unused")
class SubtypedDef implements SubtypedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          subtype_name;
private Map<String,SubtypedValue> subtype_values;
private SubtypedChecker merge_map;
private SubtypedChecker restrict_map;
private List<SubtypedOpCheck> op_checks;
private SubtypedChecker predecessor_map;
private List<SubtypedInstanceCheck> const_checks;
private List<SubtypedInstanceCheck> uninit_checks;
private List<SubtypedInstanceCheck> default_checks;
private List<SubtypedInstanceCheck> base_checks;
private SubtypedValue default_constant;
private SubtypedValue default_uninit;
private SubtypedValue default_value;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SubtypedDef(Element xml) 
{
   subtype_name = IvyXml.getAttrString(xml,"NAME");
   
   default_constant = null;
   default_uninit = null;
   default_value = null;
   subtype_values = new HashMap<>();
   SubtypedValue first = null;
   for (Element valxml : IvyXml.children(xml,"VALUE")) {
      SubtypedValue v = new SubtypedValue(valxml);
      if (first == null) first = v;
      subtype_values.put(v.getName(),v);
      if (v.isConstantDefault()) default_constant = v;
      if (v.isUninitDefault()) default_uninit = v;
      if (v.isDefault()) default_value = v;
    }
   if (default_value == null) default_value = first;
   if (default_uninit == null) default_uninit = first;
   if (default_constant == null) default_constant = first;
   
   merge_map = new SubtypedChecker();
   for (Element mrgxml : IvyXml.children(xml,"MERGE")) {
      SubtypedValue v1 = getValue(IvyXml.getAttrString(mrgxml,"VALUE"));
      SubtypedValue v2 = getValue(IvyXml.getAttrString(mrgxml,"WITH"));
      SubtypedValue v3 = getValue(IvyXml.getAttrString(mrgxml,"YIELDS"));
      merge_map.addMapping(v1,v2,v3);
      merge_map.addMapping(v2,v1,v3);
      merge_map.addMapping(v1,v1,v1);
      merge_map.addMapping(v2,v2,v2);
    }
   
   restrict_map = new SubtypedChecker();
   for (Element rstxml : IvyXml.children(xml,"RESTRICT")) {
      SubtypedValue v1 = getValue(IvyXml.getAttrString(rstxml,"VALUE"));
      SubtypedValue v2 = getValue(IvyXml.getAttrString(rstxml,"WITH"));
      SubtypedValue v3 = getValue(IvyXml.getAttrString(rstxml,"YIELDS"));
      if (v3 != null) {
         restrict_map.addMapping(v1,v2,v3);
       }
      else {
         String err = IvyXml.getAttrString(rstxml,"ERROR");
         String wrn = IvyXml.getAttrString(rstxml,"WARNING");
         String note = IvyXml.getAttrString(rstxml,"NOTE");
         if (err != null) {
            restrict_map.addMapping(v1,v2,ErrorLevel.ERROR,err);
          }
         else if (wrn != null) {
            restrict_map.addMapping(v1,v2,ErrorLevel.WARNING,wrn);
          }
         else if (note != null) {
            restrict_map.addMapping(v1,v2,ErrorLevel.NOTE,note);
          }
       }
    }
   
   const_checks = getInstanceChecks(xml,"CONSTANT");
   uninit_checks = getInstanceChecks(xml,"UNINITIALIZED");
   default_checks = getInstanceChecks(xml,"DEFUALT");
   base_checks = getInstanceChecks(xml,"BASE");
   
   op_checks = new ArrayList<>();
   for (Element opxml : IvyXml.children(xml,"OPERATION")) {
      SubtypedOpCheck ock = new SubtypedOpCheck(this,opxml);
      op_checks.add(ock);
    }
   
   predecessor_map = new SubtypedChecker();
   for (Element prdxml : IvyXml.children(xml,"PREDECSSOR")) {
      SubtypedValue v1 = getValue(IvyXml.getAttrString(prdxml,"CURRENT"));
      SubtypedValue v2 = getValue(IvyXml.getAttrString(prdxml,"PREDECESSOR"));
      boolean fg = IvyXml.getAttrBool(prdxml,"RESULT");
      predecessor_map.addMapping(v1,v2,fg);
    }
}



SubtypedDef(String name) 
{
   subtype_name = name;
   default_constant = null;
   default_uninit = null;
   default_value = null;
   subtype_values = new HashMap<>();
   merge_map = null;
   restrict_map = null;
   op_checks = null;
   predecessor_map = null;
   const_checks = null;
   uninit_checks = null;
   default_checks = null;
   base_checks = null;
   default_constant = null;
   default_uninit = null;
   default_value = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

String getName()                                { return subtype_name; }

Collection<SubtypedValue> getValues()           { return subtype_values.values(); }

SubtypedValue getValue(String name)           
{
   if (name == null) return null;
   
   SubtypedValue v = subtype_values.get(name);
   if (v != null) return v;
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Loading methods                                                         */
/*                                                                              */
/********************************************************************************/

private List<SubtypedInstanceCheck> getInstanceChecks(Element xml,String what)
{
   List<SubtypedInstanceCheck> rslt = null;
   for (Element chkxml : IvyXml.children(xml,what)) {
      if (rslt == null) rslt = new ArrayList<>();
      SubtypedInstanceCheck sic = new SubtypedInstanceCheck(this,chkxml);
      rslt.add(sic);
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String toString()
{
   return getName();
}


}       // end of class SubtypedDef




/* end of SubtypedDef.java */


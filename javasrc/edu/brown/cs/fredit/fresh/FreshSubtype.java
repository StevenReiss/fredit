/********************************************************************************/
/*                                                                              */
/*              FreshSubtype.java                                               */
/*                                                                              */
/*      Representation of an editable subtype definition                        */
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSubtype implements FreshConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          subtype_name;
private boolean         is_enabled;
private Map<String,FreshSubtypeValue> subtype_values;
private FreshSubtypeChecker merge_map;
private FreshSubtypeChecker restrict_map;
private FreshSubtypeChecker predecessor_map;
private List<FreshSubtypeOpCheck> op_checks;
private List<FreshSubtypeInstanceCheck> const_checks;
private List<FreshSubtypeInstanceCheck> uninit_checks;
private List<FreshSubtypeInstanceCheck> default_checks;
private List<FreshSubtypeInstanceCheck> base_checks;
private FreshSubtypeValue default_constant;
private FreshSubtypeValue default_uninit;
private FreshSubtypeValue default_value;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSubtype(Element xml) 
{
   subtype_name = IvyXml.getAttrString(xml,"NAME");
   is_enabled = IvyXml.getAttrBool(xml,"ENABLE");
   
   default_constant = null;
   default_uninit = null;
   default_value = null;
   subtype_values = new HashMap<>();
   FreshSubtypeValue first = null;
   for (Element valxml : IvyXml.children(xml,"VALUE")) {
      FreshSubtypeValue v = new FreshSubtypeValue(valxml);
      if (first == null) first = v;
      subtype_values.put(v.getName(),v);
      if (v.isConstantDefault()) default_constant = v;
      if (v.isUninitDefault()) default_uninit = v;
      if (v.isDefault()) default_value = v;
    }
   if (default_value == null) default_value = first;
   if (default_uninit == null) default_uninit = first;
   if (default_constant == null) default_constant = first;
   
   merge_map = new FreshSubtypeChecker();
   for (Element mrgxml : IvyXml.children(xml,"MERGE")) {
      FreshSubtypeValue v1 = getValue(IvyXml.getAttrString(mrgxml,"VALUE"));
      FreshSubtypeValue v2 = getValue(IvyXml.getAttrString(mrgxml,"WITH"));
      FreshSubtypeValue v3 = getValue(IvyXml.getAttrString(mrgxml,"YIELDS"));
      merge_map.addMapping(v1,v2,v3);
      merge_map.addMapping(v2,v1,v3);
      merge_map.addMapping(v1,v1,v1);
      merge_map.addMapping(v2,v2,v2);
    }
   
   restrict_map = new FreshSubtypeChecker();
   for (Element rstxml : IvyXml.children(xml,"RESTRICT")) {
      FreshSubtypeValue v1 = getValue(IvyXml.getAttrString(rstxml,"VALUE"));
      FreshSubtypeValue v2 = getValue(IvyXml.getAttrString(rstxml,"WITH"));
      FreshSubtypeValue v3 = getValue(IvyXml.getAttrString(rstxml,"YIELDS"));
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
   default_checks = getInstanceChecks(xml,"DEFAULT");
   base_checks = getInstanceChecks(xml,"BASE");
   
   op_checks = new ArrayList<>();
   for (Element opxml : IvyXml.children(xml,"OPERATION")) {
      FreshSubtypeOpCheck ock = new FreshSubtypeOpCheck(this,opxml);
      op_checks.add(ock);
    }
   
   predecessor_map = new FreshSubtypeChecker();
   for (Element prdxml : IvyXml.children(xml,"PREDECESSOR")) {
      FreshSubtypeValue v1 = getValue(IvyXml.getAttrString(prdxml,"CURRENT"));
      FreshSubtypeValue v2 = getValue(IvyXml.getAttrString(prdxml,"PREDECESSOR"));
      boolean fg = IvyXml.getAttrBool(prdxml,"RESULT");
      predecessor_map.addMapping(v1,v2,fg);
    }
}



FreshSubtype(String name) 
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

Collection<FreshSubtypeValue> getValues()           { return subtype_values.values(); }

FreshSubtypeValue getValue(String name)           
{
   if (name == null) return null;
   
   FreshSubtypeValue v = subtype_values.get(name);
   if (v != null) return v;
   return null;
}




/********************************************************************************/
/*                                                                              */
/*      Loading methods                                                         */
/*                                                                              */
/********************************************************************************/

private List<FreshSubtypeInstanceCheck> getInstanceChecks(Element xml,String what)
{
   List<FreshSubtypeInstanceCheck> rslt = null;
   for (Element chkxml : IvyXml.children(xml,what)) {
      if (rslt == null) rslt = new ArrayList<>();
      FreshSubtypeInstanceCheck sic = new FreshSubtypeInstanceCheck(this,chkxml);
      rslt.add(sic);
    }
   return rslt;
}



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw) 
{
   xw.begin("SUBTYPE");
   
   xw.field("NAME",subtype_name);
   xw.field("ENABLE",is_enabled);
   for (FreshSubtypeValue value : subtype_values.values()) {
      value.outputXml(xw);
    }
   merge_map.outputXml("MERGE",xw);
   restrict_map.outputXml("RESTRICT",xw);
   predecessor_map.outputXml("PREDECESSOR",xw);
   for (FreshSubtypeOpCheck opchk : op_checks) {
      opchk.outputXml(xw);
    }
   if (const_checks != null) {
      for (FreshSubtypeInstanceCheck chk : const_checks) {
         chk.outputXml("CONSTANT",xw);
       }
    }
   if (uninit_checks != null) {
      for (FreshSubtypeInstanceCheck chk : uninit_checks) {
         chk.outputXml("UNINITIALIZED",xw);
       }
    }
   if (default_checks != null) {
      for (FreshSubtypeInstanceCheck chk : default_checks) {
         chk.outputXml("DEFAULT",xw);
       }
    }
   if (base_checks != null) {
      for (FreshSubtypeInstanceCheck chk : base_checks) {
         chk.outputXml("BASE",xw);
       }
    }
   
   xw.end("SUBTYPE");
}



@Override public String toString()
{
   return getName();
}



}       // end of class FreshSubtype




/* end of FreshSubtype.java */


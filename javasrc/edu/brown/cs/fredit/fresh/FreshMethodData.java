/********************************************************************************/
/*                                                                              */
/*              FreshMethodData.java                                            */
/*                                                                              */
/*      Information about a method/class/package                                */
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;


public class FreshMethodData implements FreshConstants, FreshConstants.FreshSkipItem, Cloneable
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String          base_name;
private MethodDataKind  base_kind;

private String          replace_name;
private int             return_arg;
private String          result_type;
private String          alt_result;
private List<String>    throw_types;
private boolean         canbe_null;
private List<String>    type_annots;
private Map<Integer,List<String>> arg_annots;
private boolean         is_mutable;
private boolean         does_exit;
private boolean         async_call;
private boolean         is_constructor;
private boolean         no_return;
private boolean         no_virtual;
private boolean         set_fields;
private boolean         is_affected;
private InlineType      inline_type;
private boolean         is_clone;
private boolean         dont_scan;
private List<String>    callback_names;
private String          callback_id;
private List<String>    callback_args;
private List<FreshMethodWhen> when_conditions;
private Set<String>     load_types;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

// CHECKSTYLE:OFF
FreshMethodData(Element xml)
// CHECKSTYLE:ON
{
   base_name = IvyXml.getAttrString(xml,"NAME");
   boolean formthd = false;
   if (IvyXml.isElement(xml,"PACKAGE")) {
      base_kind = MethodDataKind.PACKAGE;
      if (!base_name.endsWith(".")) base_name += ".";
    }
   else if (IvyXml.isElement(xml,"CLASS")) {
      base_kind = MethodDataKind.CLASS;
      if (!base_name.endsWith(".")) base_name += ".";
    }
   else {                       // METHOD
      String sgn = IvyXml.getAttrString(xml,"SIGNATURE");
      formthd = true;
      if (sgn == null) base_kind = MethodDataKind.METHOD;
      else {
         base_kind = MethodDataKind.FULL_METHOD;
         base_name += sgn;
       }
    }
   
   replace_name = IvyXml.getTextElement(xml,"REPLACE");
   
   return_arg = -1;
   result_type = null;
   alt_result = null;
   String rtn = IvyXml.getAttrString(xml,"RETURN");
   if (rtn == null || rtn == "" || rtn.equals("*")) ;
   else if (rtn.equals("0")) return_arg = 0;
   else if (rtn.equals("1")) return_arg = 1;
   else {
      result_type = rtn;
    }
   alt_result = IvyXml.getAttrString(xml,"ARETURN");
   throw_types = null;
   String thr = IvyXml.getAttrString(xml,"THROWS");
   if (thr != null) {
      StringTokenizer tok = new StringTokenizer(thr);
      while (tok.hasMoreTokens()) {
	 String th = tok.nextToken();
	 if (throw_types == null) throw_types = new ArrayList<>();
	 throw_types.add(th);
       }
    }
   
   canbe_null = IvyXml.getAttrBool(xml,"NULL",!formthd);
   String annots = IvyXml.getAttrString(xml,"ANNOTATIONS");
   type_annots = new ArrayList<>();
   if (annots != null) {
      StringTokenizer tok = new StringTokenizer(annots," ,");
      while (tok.hasMoreTokens()) {
	 type_annots.add(tok.nextToken());
       }
      if (!canbe_null && !type_annots.contains("NonNull")) type_annots.add("NonNull");
    }
   else {
      if (canbe_null) type_annots.add("Nullable");
      else type_annots.add("NonNull");
    }
   
   arg_annots = new HashMap<>();
   String argannot = IvyXml.getAttrString(xml,"ARGANNOTATIONS");
   if (argannot != null) {
      StringTokenizer tok = new StringTokenizer(argannot,";+ ");
      while (tok.hasMoreTokens()) {
	 String aan = tok.nextToken();
	 List<String> aaset = new ArrayList<>();
	 int idx = aan.indexOf(":");
	 if (idx < 0) continue;
	 String id = aan.substring(0,idx);
	 aan = aan.substring(idx+1);
	 int ano = -1;
	 try {
	    ano = Integer.parseInt(id);
	  }
	 catch (NumberFormatException e) {
	    continue;
	 }
	 StringTokenizer tok1 = new StringTokenizer(aan,",@");
	 while (tok1.hasMoreTokens()) {
	    aaset.add(tok1.nextToken());
	  }
	 arg_annots.put(ano,aaset);
       }
    }
   
   is_mutable = IvyXml.getAttrBool(xml,"MUTABLE",!formthd);
   does_exit = IvyXml.getAttrBool(xml,"EXIT");
   async_call = IvyXml.getAttrBool(xml,"ASYNC");
   is_constructor = IvyXml.getAttrBool(xml,"CONSTRUCTOR");
   no_return = IvyXml.getAttrBool(xml,"NORETURN");
   no_virtual = IvyXml.getAttrBool(xml,"NOVIRTUAL");
   set_fields = IvyXml.getAttrBool(xml,"SETFIELDS");
   is_affected = IvyXml.getAttrBool(xml,"AFFECTED");
   inline_type = IvyXml.getAttrEnum(xml,"INLINE",InlineType.NORMAL);
   is_clone = IvyXml.getAttrBool(xml,"CLONE");
   
   dont_scan = true;
   if (inline_type != InlineType.NORMAL) dont_scan = false;
   dont_scan = !IvyXml.getAttrBool(xml,"SCAN",!dont_scan);
   
   callback_names = null;
   callback_args = null;
   callback_id = null;
   String cbnm = IvyXml.getTextElement(xml,"CALLBACK");
   if (cbnm != null) {
      callback_names = new ArrayList<>();
      callback_id = IvyXml.getAttrString(xml,"CBID");
      for (StringTokenizer tok = new StringTokenizer(cbnm); tok.hasMoreTokens(); ) {
	 String cn = tok.nextToken();
	 callback_names.add(cn);
       }
      String args = IvyXml.getAttrString(xml,"CBARGS");
      if (args == null) args = "1";
      callback_args = scanArgs(args);
    }
   
   if (is_constructor) {
      callback_args = new ArrayList<>();
      String args = IvyXml.getAttrString(xml,"ARGS");
      if (args == null) args = "*";
      callback_args = scanArgs(args);
    }
   
   if (callback_args == null) {
      String args = IvyXml.getAttrString(xml,"ARGS");
      if (args != null) callback_args = scanArgs(args);
    }
   
   when_conditions = null;
   for (Element welt : IvyXml.children(xml,"WHEN")) {
      if (when_conditions == null) when_conditions = new ArrayList<>();
      FreshMethodWhen wh = new FreshMethodWhen(welt);
      when_conditions.add(wh);
    }
   
   load_types = null;
   for (Element lelt : IvyXml.children(xml,"LOAD")) {
      if (load_types == null) load_types = new HashSet<>();
      String nm = IvyXml.getAttrString(lelt,"NAME");
      if (nm == null) nm = IvyXml.getText(lelt);
      if (nm != null) load_types.add(nm);
    }
   
   if (replace_name != null) {
      StringTokenizer tok = new StringTokenizer(replace_name);
      while (tok.hasMoreTokens()) {
	 String mthd = tok.nextToken();
	 int idx = mthd.indexOf("(");
	 if (idx > 0) mthd = mthd.substring(0,idx);
	 if (!is_constructor) {
	    idx = mthd.lastIndexOf(".");
	    if (idx > 0) mthd = mthd.substring(0,idx);
	  }
	 if (load_types == null) load_types = new HashSet<>();
	 load_types.add(mthd);
       }
    }
}


FreshMethodData(String name,MethodDataKind kind)
{
   // might need to handle methods different than class/packages
   base_name = name;
   base_kind = kind;
   replace_name = null;
   return_arg = -1;
   result_type = null;
   alt_result = null;
   throw_types = null;
   canbe_null = true;
   type_annots = new ArrayList<>();
   arg_annots = new HashMap<>();
   is_mutable = true;
   does_exit = false;
   async_call = false;
   is_constructor = false;
   no_return = false;
   no_virtual = false;
   set_fields = false;
   is_affected = false;
   inline_type = InlineType.NORMAL;
   is_clone = false;
   dont_scan = true;
   callback_names = null;
   callback_args = null;
   callback_id = null;
   when_conditions = null;
   load_types = null;
}



@Override public FreshMethodData clone()
{
   try {
      FreshMethodData fmd = (FreshMethodData) super.clone();
      return fmd;
    }
   catch (CloneNotSupportedException e) {
      return null;
    }
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public MethodDataKind getKind()               { return base_kind; }
@Override public String getName()                       { return base_name; }

public boolean isSkipped()
{
   if (dont_scan) return true;
  
   return false;
}

void setSkipped(boolean fg) 
{
   dont_scan = !fg;
}


/********************************************************************************/
/*										*/
/*	Argument Encoding							*/
/*										*/
/********************************************************************************/

private List<String> scanArgs(String coding)
{
   List<String> rslt = new ArrayList<>();
   
   for (StringTokenizer tok = new StringTokenizer(coding," \t,"); tok.hasMoreTokens(); ) {
      String nvl = tok.nextToken();
      rslt.add(nvl);
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
   String what = base_kind.toString();
   String name = base_name;
   String sgn = null;
   if (base_kind == MethodDataKind.FULL_METHOD) {
      what = "METHOD";
      int idx = name.indexOf("(");
      if (idx > 0) {
         sgn = name.substring(idx);
         name = name.substring(0,idx);
       }
    }
   boolean formthd = what.equals("METHOD");
   
   xw.begin(what);
   
   xw.field("NAME",name);
   if (sgn != null) xw.field("SIGNATURE",sgn);
   if (result_type != null) xw.field("RETURN",result_type);
   else if (return_arg >= 0) xw.field("RETURN",return_arg);
   if (alt_result != null) xw.field("ARETURN",alt_result);
   listToField("THROWS",throw_types,xw);
   if (canbe_null == formthd) xw.field("NULL",canbe_null);
   listToField("ANNOTATIONS",type_annots,xw);
   if (arg_annots != null) {
      StringBuffer buf = new StringBuffer();
      for (Map.Entry<Integer,List<String>> ent : arg_annots.entrySet()) {
         if (!buf.isEmpty()) buf.append(" ");
         buf.append(ent.getKey());
         buf.append(":");
         buf.append(listToString(ent.getValue()));
       }
      if (!buf.isEmpty()) xw.field("ARGANNOTATIONS",buf.toString());      
    }
   if (is_mutable == formthd) xw.field("MUTABLE",is_mutable);
   if (does_exit) xw.field("EXIT",does_exit);
   if (async_call) xw.field("ASYNC",async_call);
   if (is_constructor) xw.field("CONSTRUCTOR",is_constructor);
   if (no_return) xw.field("NORETURN",no_return);
   if (no_virtual) xw.field("NOVIRTUAL",no_virtual);
   if (set_fields) xw.field("SETFIELDS",set_fields);
   if (is_affected) xw.field("AFFECTED",is_affected);
   if (inline_type != InlineType.NORMAL) xw.field("INLINE",inline_type);
   if (is_clone) xw.field("CLONE",is_clone);
   
   boolean noscan = true;
   if (inline_type != InlineType.NORMAL) noscan = false;
   if (dont_scan != noscan) xw.field("SCAN",!dont_scan);
   
   if (callback_id != null) xw.field("CBID",callback_id);
   if (is_constructor) listToField("ARGS",callback_args,xw);
   else if (callback_id != null) listToField("CBARGS",callback_args,xw);
   else listToField("ARGS",callback_args,xw);
   
   // end of fields
   
   if (replace_name != null) xw.textElement("REPLACE",replace_name);
   listToTextElement("CALLBACK",callback_names,xw);
   if (load_types != null) {
      for (String load : load_types) {
         xw.textElement("LOAD",load);
       }
    }
   if (when_conditions != null) {
      for (FreshMethodWhen when : when_conditions) {
         when.outputXml(xw);
       }
    }
   
   xw.end(what);
}


}       // end of class FreshMethodData




/* end of FreshMethodData.java */


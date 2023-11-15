/********************************************************************************/
/*                                                                              */
/*              FreshConstants.java                                             */
/*                                                                              */
/*      Constants for Fait RESource Helper                                      */
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

import java.util.List;

import edu.brown.cs.ivy.xml.IvyXmlWriter;

public interface FreshConstants
{

enum ErrorLevel {
   NONE,
   NOTE,
   WARNING,
   ERROR
}


enum MethodDataKind {
   PACKAGE,
   CLASS,
   METHOD,
   FULL_METHOD
}


enum InlineType {
   NONE,                                // don't inline
   DEFAULT,                             // inline based on source set
   THIS,                                // inline based on this argument
   SOURCES,                             // inline based on all sources
   VALUES,                              // inline based on all values
   SPECIAL,                             // based on special type
   NORMAL,                              // using normal rules
}

enum AllocMode {
   LOCAL,
   BINARY,
   BINARY_INHERIT,
   AST,
   AST_INHERIT,
}



/********************************************************************************/
/*                                                                              */
/*      Helper methods                                                          */
/*                                                                              */
/********************************************************************************/

default String listToString(List<?> data)
{
   return listToString(data," ");
}
   

default String listToString(List<?> data,String sep)
{
   if (data == null) return null;
   
   StringBuffer buf = new StringBuffer();
   for (Object v : data) {
      if (!buf.isEmpty()) buf.append(sep);
      buf.append(v.toString());
    }
   if (buf.isEmpty()) return null;
   
   return buf.toString();
}


default void listToField(String key,List<?> data,IvyXmlWriter xw)
{
   String val = listToString(data);
   if (val != null) xw.field(key,val);
}


default void listToTextElement(String key,List<?> data,IvyXmlWriter xw)
{
   String val = listToString(data);
   if (val != null) xw.textElement(key,val);
}


}       // end of interface FreshConstants




/* end of FreshConstants.java */

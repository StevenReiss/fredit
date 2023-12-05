/********************************************************************************/
/*                                                                              */
/*              FreshSafetyState.java                                           */
/*                                                                              */
/*      Represent a state in a safety condition                                 */
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

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;
import edu.brown.cs.ivy.xml.IvyXmlWriter;

public class FreshSafetyStateImpl implements FreshConstants, FreshConstants.FreshSafetyState
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String state_name;
private boolean is_initial;



/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/***********************`*********************************************************/

FreshSafetyStateImpl(Element xml) 
{
   state_name = IvyXml.getAttrString(xml,"NAME");
   is_initial = IvyXml.getAttrBool(xml,"INITIAL");
}

FreshSafetyStateImpl(String name,boolean init)
{
   state_name = name;
   is_initial = init;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public String getName()                    { return state_name; }

@Override public boolean isInitial()                 { return is_initial; }



/********************************************************************************/
/*                                                                              */
/*      Output methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(Collection<FreshSafetyTransitionImpl> transet,IvyXmlWriter xw) 
{
   xw.begin("STATE");
   xw.field("NAME",state_name);
   if (is_initial) xw.field("INITIAL",is_initial);
   for (FreshSafetyTransitionImpl trans : transet) {
      if (trans.getFromState() == this) {
         trans.outputXml(xw);
       }
    }
}



@Override public String toString()
{
   return getName();
}


}       // end of class FreshSafetyState




/* end of FreshSafetyState.java */


/********************************************************************************/
/*                                                                              */
/*              FreshSafetyTransition.java                                      */
/*                                                                              */
/*      Representation of a transition for a safety condition                   */
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

public class FreshSafetyTransitionImpl implements FreshConstants, FreshConstants.FreshSafetyTransition
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String on_event;
private String error_message;
private ErrorLevel error_level;
private FreshSafetyStateImpl  from_state;
private FreshSafetyStateImpl  to_state;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

FreshSafetyTransitionImpl(FreshSafetyConditionImpl cond,FreshSafetyStateImpl from,Element onxml) 
{
   from_state = from;
   on_event = IvyXml.getAttrString(onxml,"EVENT");
   cond.defineEvent(on_event);
   String next = IvyXml.getAttrString(onxml,"GOTO");
   to_state = cond.getState(next);
   
   error_level = ErrorLevel.NONE;
   error_message = null;
   String err = IvyXml.getTextElement(onxml,"ERROR");
   String warn = IvyXml.getTextElement(onxml,"WARNING");
   String note = IvyXml.getTextElement(onxml,"NOTE");
   if (err != null) {
      error_message = err;
      error_level = ErrorLevel.ERROR;
    }
   else if (warn != null) {
      error_message = warn;
      error_level = ErrorLevel.WARNING; 
    }
   else if (note != null) {
      error_message = note;
      error_level = ErrorLevel.NOTE;
    }
}



FreshSafetyTransitionImpl(FreshSafetyStateImpl fromstate,FreshSafetyStateImpl tostate) 
{
   from_state = fromstate;
   to_state = tostate;
   on_event = null;          // default
   error_message = null;
   error_level = null;
}



FreshSafetyTransitionImpl(FreshSafetyStateImpl tostate,String evt) 
{
   from_state = null;
   to_state = tostate;
   on_event = evt;
   error_message = null;
   error_level = null;
}



/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

@Override public FreshSafetyStateImpl getFromState()            { return from_state; }
@Override public FreshSafetyStateImpl getToState()              { return to_state; }
@Override public String getEvent()                              { return on_event; }
@Override public ErrorLevel getErrorLevel()             { return error_level; }
@Override public String getErrorMessage()               { return error_message; }


boolean hasMessage() 
{
   if (error_level != null && 
         error_level != ErrorLevel.NONE && 
         error_message != null) 
      return true;
   
   return false;
}



/********************************************************************************/
/*                                                                              */
/*      Output Methods                                                          */
/*                                                                              */
/********************************************************************************/

void outputXml(IvyXmlWriter xw)
{
   xw.begin("ON");
   
   xw.field("EVENT",on_event);
   xw.field("GOTO",to_state.getName());
   if (error_message != null) {
      xw.textElement(error_level.toString(),error_message);
    }
   
   xw.end("ON");
}



}       // end of class FreshSafetyTransition




/* end of FreshSafetyTransition.java */


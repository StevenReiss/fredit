/********************************************************************************/
/*                                                                              */
/*              SafetyedTransition.java                                         */
/*                                                                              */
/*      Transition in a safety condition                                        */
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



package edu.brown.cs.fredit.safetyed;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

class SafetyedTransition implements SafetyedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String on_event;
private String error_message;
private ErrorLevel error_level;
private SafetyedState  from_state;
private SafetyedState  to_state;


/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SafetyedTransition(SafetyedCondition cond,SafetyedState from,Element onxml) 
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



SafetyedTransition(SafetyedState fromstate,SafetyedState tostate) 
{
   from_state = fromstate;
   to_state = tostate;
   on_event = null;          // default
   error_message = null;
   error_level = null;
}



SafetyedTransition(SafetyedState tostate,String evt) 
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

SafetyedState getFromState()                    { return from_state; }
SafetyedState getToState()                      { return to_state; }
String getEvent()                               { return on_event; }
ErrorLevel getErrorLevel()                      { return error_level; }
String getErrorMessage()                        { return error_message; }


boolean hasMessage() 
{
   if (error_level != null && 
         error_level != ErrorLevel.NONE && 
         error_message != null) 
      return true;
   return false;
}



}       // end of class SafetyedTransition




/* end of SafetyedTransition.java */


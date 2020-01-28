/********************************************************************************/
/*                                                                              */
/*              SafetyedCondition.java                                          */
/*                                                                              */
/*      Holder of a safety condition                                            */
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import edu.brown.cs.ivy.xml.IvyXml;

class SafetyedCondition implements SafetyedConstants
{


/********************************************************************************/
/*                                                                              */
/*      Private Storage                                                         */
/*                                                                              */
/********************************************************************************/

private String condition_name;
private Map<String,SafetyedState> state_set;
private Set<String> event_names;
private List<SafetyedTransition> all_transitions;




/********************************************************************************/
/*                                                                              */
/*      Constructors                                                            */
/*                                                                              */
/********************************************************************************/

SafetyedCondition(Element xml) 
{
   condition_name = IvyXml.getAttrString(xml,"NAME");
   
   event_names = new HashSet<>();
   
   state_set = new HashMap<>();
   for (Element statexml : IvyXml.children(xml,"STATE")) {
      SafetyedState s = new SafetyedState(statexml);
      state_set.put(s.getName(),s);
    }
   
   all_transitions = new ArrayList<>();
   for (Element statexml : IvyXml.children(xml,"STATE")) {
      String nm = IvyXml.getAttrString(statexml,"NAME");
      SafetyedState from = state_set.get(nm);
      for (Element onxml : IvyXml.children(statexml,"ON")) {
         SafetyedTransition t = new SafetyedTransition(this,from,onxml);
         all_transitions.add(t);
         Element elsexml = IvyXml.getChild(statexml,"ELSE");
         if (elsexml != null) {
            String next = IvyXml.getAttrString(elsexml,"GOTO");
            if (next != null && !next.equals("*")) {
               SafetyedState to = state_set.get(next);
               SafetyedTransition dflt = new SafetyedTransition(from,to);
               all_transitions.add(dflt);
             }
          }
       }
    }
   
   fixDefaultStateTransitions();
}



SafetyedCondition(String nm)
{
   condition_name = nm;
   event_names = new HashSet<>();
   state_set = new HashMap<>();
   all_transitions = new ArrayList<>();
   
   SafetyedState initial = new SafetyedState("INITIAL",true);
   state_set.put(initial.getName(),initial);
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

SafetyedState getState(String name)
{
   return state_set.get(name);
}



Collection<SafetyedTransition> getTransitions()
{
   return all_transitions;
}


Collection<SafetyedState> getStates()
{
   return state_set.values();
}


Collection<String> getEvents()
{
   return event_names;
}


/********************************************************************************/
/*                                                                              */
/*      Creation methods                                                        */
/*                                                                              */
/********************************************************************************/

void defineEvent(String evt)
{
   event_names.add(evt);
}


private void fixDefaultStateTransitions()
{
   // find default transitions where EVENT always goes to STATE w/o error
   for (String evt : event_names) {
      List<SafetyedTransition> remtrans = new ArrayList<>();
      SafetyedState target = null;
      for (SafetyedState state : state_set.values()) {
         SafetyedTransition usetrans = null;
         for (SafetyedTransition trans : all_transitions) {
            if (trans.getFromState() == null || trans.getFromState() == state) {
               if (trans.getEvent() == null || trans.getEvent().equals(evt)) {
                  usetrans = trans;
                  break;
                }
             }
          }
         SafetyedState to = state;
         if (usetrans != null) to = usetrans.getToState();
         if (target == null) target = to;
         if (target != to || (usetrans != null && usetrans.hasMessage())) {
            remtrans = null;
            break;
          }
         else if (usetrans != null && usetrans.getFromState() != null &&
               usetrans.getEvent() != null) {
            remtrans.add(usetrans);
          }
       }
      if (remtrans != null) {
         SafetyedTransition newtrans = new SafetyedTransition(target,evt);
         all_transitions.add(newtrans);
         all_transitions.removeAll(remtrans);
       }
    }
}




/********************************************************************************/
/*                                                                              */
/*      Access methods                                                          */
/*                                                                              */
/********************************************************************************/

public String getName()                         { return condition_name; }


@Override public String toString()              // for list output
{
   return getName();
}



}       // end of class SafetyedCondition




/* end of SafetyedCondition.java */

